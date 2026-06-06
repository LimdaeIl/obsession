package com.app.obsession.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenHashUtilTest {

    private final TokenHashUtil tokenHashUtil = new TokenHashUtil();

    @Test
    @DisplayName("같은 토큰은 항상 같은 SHA-256 hash를 만든다")
    void sha256_sameInputSameHash() {
        String token = "sample-token";

        String hash1 = tokenHashUtil.sha256(token);
        String hash2 = tokenHashUtil.sha256(token);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("다른 토큰은 다른 SHA-256 hash를 만든다")
    void sha256_differentInputDifferentHash() {
        String hash1 = tokenHashUtil.sha256("token-1");
        String hash2 = tokenHashUtil.sha256("token-2");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("SHA-256 hash는 64자리 hex 문자열이다")
    void sha256_hexLength() {
        String hash = tokenHashUtil.sha256("sample-token");

        assertThat(hash).hasSize(64);
        assertThat(hash).matches("^[a-f0-9]{64}$");
    }
}
