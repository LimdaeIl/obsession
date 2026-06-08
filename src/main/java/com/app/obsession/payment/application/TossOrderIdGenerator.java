package com.app.obsession.payment.application;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class TossOrderIdGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final char[] CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(Long orderId) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String random = randomText(8);

        return "ORDER-" + orderId + "-" + timestamp + "-" + random;
    }

    private String randomText(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(CHARS[secureRandom.nextInt(CHARS.length)]);
        }

        return builder.toString();
    }
}
