package com.app.obsession.payment.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
        String secretKey,
        String baseUrl
) {

}
