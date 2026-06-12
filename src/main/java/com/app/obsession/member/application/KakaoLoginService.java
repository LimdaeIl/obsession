package com.app.obsession.member.application;

import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.application.port.SocialAccountRepository;
import com.app.obsession.member.application.result.LoginResult;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.domain.SocialAccount;
import com.app.obsession.member.domain.SocialProvider;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import com.app.obsession.member.infrastructure.external.KakaoOAuthClient;
import com.app.obsession.member.infrastructure.external.KakaoTokenResponse;
import com.app.obsession.member.infrastructure.external.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final LoginService loginService;

    @Transactional
    public LoginResult login(String code) {
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.requestToken(code);
        KakaoUserInfoResponse userInfo = kakaoOAuthClient.requestUserInfo(
                tokenResponse.accessToken());

        String providerId = String.valueOf(userInfo.id());
        String email = userInfo.email();
        String name = userInfo.nickname();

        validateKakaoEmail(userInfo);

        return socialAccountRepository
                .findByProviderAndProviderId(SocialProvider.KAKAO, providerId)
                .map(this::loginExistingSocialMember)
                .orElseGet(() -> signupOrLinkAndLogin(email, name, providerId));
    }

    private LoginResult loginExistingSocialMember(SocialAccount socialAccount) {
        Member member = memberRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateLoginAvailable(member);

        return loginService.issueToken(member);
    }

    private LoginResult signupOrLinkAndLogin(String email, String name, String providerId) {
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        Member.createSocialCustomer(name, email, null)
                ));

        validateLoginAvailable(member);
        validateSocialAccountLinkable(member);

        SocialAccount socialAccount = SocialAccount.create(
                member.getId(),
                SocialProvider.KAKAO,
                providerId
        );

        socialAccountRepository.save(socialAccount);

        return loginService.issueToken(member);
    }

    private void validateSocialAccountLinkable(Member member) {
        socialAccountRepository.findByMemberId(member.getId())
                .ifPresent(existingSocialAccount -> {
                    throw new MemberException(MemberErrorCode.ALREADY_LINKED_SOCIAL_ACCOUNT);
                });
    }

    private void validateLoginAvailable(Member member) {
        if (member.isWithdrawn()) {
            throw new MemberException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }

    private void validateKakaoEmail(KakaoUserInfoResponse userInfo) {
        if (userInfo.email() == null || userInfo.email().isBlank()) {
            throw new MemberException(MemberErrorCode.SOCIAL_EMAIL_NOT_PROVIDED);
        }

        if (!Boolean.TRUE.equals(userInfo.kakaoAccount().isEmailValid())
                || !Boolean.TRUE.equals(userInfo.kakaoAccount().isEmailVerified())) {
            throw new MemberException(MemberErrorCode.SOCIAL_EMAIL_NOT_VERIFIED);
        }
    }
}
