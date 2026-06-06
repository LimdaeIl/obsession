package com.app.obsession.member.application;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.global.security.jwt.BearerTokenResolver;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.port.AccessTokenBlacklistRepository;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final BearerTokenResolver bearerTokenResolver;
    private final TokenInvalidationService tokenInvalidationService;

    public void logout(Long memberId, String authorizationHeader) {
        String accessToken = bearerTokenResolver.resolve(authorizationHeader);

        tokenInvalidationService.invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );
    }
}
