package com.app.obsession.payment.infrastructure.external;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

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
            throw new IllegalStateException(
                    "토스 결제 승인 실패: status=" + e.getStatusCode()
                            + ", body=" + e.getResponseBodyAsString()
            );
        }
    }

    private static String authorizationHeader(String secretKey) {
        String credential = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encoded;
    }
}
