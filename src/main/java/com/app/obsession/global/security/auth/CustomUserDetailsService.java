package com.app.obsession.global.security.auth;

import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return memberRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public UserDetails loadUserByMemberId(Long memberId) {
        throw new UnsupportedOperationException("현재 JWT 인증은 JwtAuthenticationFilter에서 직접 UserDetails를 생성합니다.");
    }
}
