package com.payment.stripeintegration.payment;

import com.payment.stripeintegration.payment.config.StripeConfigProperties;
import com.payment.stripeintegration.payment.model.CreatePaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public record PaymentService(StripeConfigProperties stripeConfigProperties) {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    @PostConstruct
    public void setUp() {
        Stripe.apiKey = stripeConfigProperties.getStripeApiKey();
    }

    public CreatePaymentResponse getCreatePaymentResponse() {
        try {
            var createParams = new PaymentIntentCreateParams.Builder()
                    .setCurrency("usd")
                    .setAmount(15 * 100L)
                    .build();

            PaymentIntent intent = PaymentIntent.create(createParams);
            return new CreatePaymentResponse(intent.getClientSecret());
        } catch (StripeException e) {
            LOGGER.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage());

        }
    }

    public String stripeWebHook(String sigHeader, String payload) {
        if (Objects.isNull(sigHeader)) return "";

        var event = validateWebHookSignature(sigHeader, payload);
        var dataObjectDeserializer = event.getDataObjectDeserializer();

        Optional<StripeObject> object = dataObjectDeserializer.getObject();
        object.ifPresent(stripeObject -> handleStripePaymentEvent(event, stripeObject));
        return "";
    }

    private void handleStripePaymentEvent(Event event, StripeObject stripeObject) {
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            LOGGER.info("Payment for {} succeeded.", paymentIntent.getAmount());

        } else {
            LOGGER.warn("Unhandled event type:  {}", event.getType());
        }
    }

    private Event validateWebHookSignature(String sigHeader, String payload) {
        try {
            return Webhook.constructEvent(payload, sigHeader, stripeConfigProperties.getWebHookSecret());
        } catch (SignatureVerificationException e) {
            LOGGER.error("⚠️  Webhook error while validating signature.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "⚠️  Webhook error while validating signature.");
        }
    }

}
