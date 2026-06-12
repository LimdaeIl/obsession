package com.app.obsession.member.application;

import com.app.obsession.global.security.jwt.JwtClaims;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.command.LoginCommand;
import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.application.port.PasswordEncryptor;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import com.app.obsession.member.application.result.LoginResult;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import com.app.obsession.member.presentation.dto.LoginResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;

    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_LOGIN));

        if (member.isWithdrawn()) {
            throw new MemberException(MemberErrorCode.WITHDRAWN_MEMBER);
        }

        if (member.isSocialOnlyMember()) {
            throw new MemberException(MemberErrorCode.SOCIAL_ONLY_MEMBER);
        }

        if (!passwordEncryptor.matches(command.password(), member.getPassword().getValue())) {
            throw new MemberException(MemberErrorCode.INVALID_LOGIN);
        }

        return issueToken(member);
    }

    @Transactional
    public LoginResult issueToken(Member member) {
        JwtClaims jwtClaims = JwtClaims.of(member);

        String accessToken = jwtProvider.createAccessToken(jwtClaims);
        String refreshToken = jwtProvider.createRefreshToken(jwtClaims);

        String refreshTokenHash = tokenHashUtil.sha256(refreshToken);

        refreshTokenRepository.saveHash(
                member.getId(),
                refreshTokenHash,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMillis())
        );

        return new LoginResult(member.getId(), accessToken, refreshToken);
    }
}
