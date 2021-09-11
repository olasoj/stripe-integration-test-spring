package com.payment.stripeintegration.payment;

import com.payment.stripeintegration.payment.model.CreatePayment;
import com.payment.stripeintegration.payment.model.CreatePaymentResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping(value = "/create-payment-intent")
    public CreatePaymentResponse createPaymentIntent(@RequestBody CreatePayment createPayment) {
        return paymentService.getCreatePaymentResponse();

    }

    @PostMapping(value = "/webhook")
    public String webhook(@RequestHeader("Stripe-Signature") String sigHeader, @RequestBody String payload) {
        return paymentService.stripeWebHook(sigHeader, payload);
    }

}
