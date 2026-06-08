package com.app.obsession.payment.infrastructure.external;

import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Slf4j(topic = "TossPaymentClient")
@Component
public class TossPaymentClient {

    private final RestClient restClient;

    public TossPaymentClient(
            TossPaymentProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader(properties.secretKey()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TossPaymentResponse confirm(
            String paymentKey,
            String orderId,
            Long amount
    ) {
        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .body(Map.of(
                            "paymentKey", paymentKey,
                            "orderId", orderId,
                            "amount", amount
                    ))
                    .retrieve()
                    .body(TossPaymentResponse.class);

        } catch (HttpStatusCodeException e) {
            log.warn("Toss payment confirm failed. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }

    private static String authorizationHeader(String secretKey) {
        String credential = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encoded;
    }
}
