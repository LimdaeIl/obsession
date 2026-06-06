package com.app.obsession.global.security.jwt;

import com.app.obsession.member.domain.Member;

public record JwtClaims(
        Long memberId,
        String role
) {

    public static JwtClaims of(Member member) {
        return new JwtClaims(
                member.getId(),
                member.getRole().name()
        );
    }
}

