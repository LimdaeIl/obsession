package com.app.obsession.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BearerTokenResolverTest {

    private final BearerTokenResolver resolver = new BearerTokenResolver();

    @Test
    @DisplayName("Bearer 토큰을 정상 추출한다")
    void resolve_success() {
        String token = resolver.resolve("Bearer access-token");

        assertThat(token).isEqualTo("access-token");
    }

    @Test
    @DisplayName("Authorization Header가 null이면 MISSING_ACCESS_TOKEN 예외")
    void resolve_nullHeader() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.MISSING_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("Authorization Header가 비어 있으면 MISSING_ACCESS_TOKEN 예외")
    void resolve_blankHeader() {
        assertThatThrownBy(() -> resolver.resolve(" "))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.MISSING_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("Bearer prefix가 없으면 INVALID_ACCESS_TOKEN 예외")
    void resolve_invalidPrefix() {
        assertThatThrownBy(() -> resolver.resolve("access-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }
}
