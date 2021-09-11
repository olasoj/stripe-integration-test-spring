package com.payment.stripeintegration.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class StripeConfigProperties {
    @Value("${app.client.payment.stripe.api-key}")
    private String stripeApiKey;

    @Value("${app.client.payment.stripe.webhook.secret}")
    private String webHookSecret;
}
