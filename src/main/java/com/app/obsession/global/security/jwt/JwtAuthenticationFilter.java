package com.app.obsession.global.security.jwt;

import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.member.application.port.AccessTokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    private final BearerTokenResolver bearerTokenResolver;
    private final JwtProvider jwtProvider;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private final TokenHashUtil tokenHashUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.clearContext();

        String accessToken = bearerTokenResolver.resolve(authorizationHeader);
        String accessTokenHash = tokenHashUtil.sha256(accessToken);

        if (accessTokenBlacklistRepository.existsByHash(accessTokenHash)) {
            throw new AuthException(AuthErrorCode.BLACKLISTED_ACCESS_TOKEN);
        }

        JwtPayload payload = jwtProvider.parseAccessPayload(accessToken);

        CustomUserDetails userDetails = new CustomUserDetails(
                payload.memberId(),
                payload.role()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
