package com.app.obsession.member.presentation.dto;

import com.app.obsession.member.domain.Member;

public record MyProfileResponse(
        Long memberId,
        String name,
        String email,
        String phone,
        String role,
        String status
) {

    public static MyProfileResponse from(Member member) {
        return new MyProfileResponse(
                member.getId(),
                member.getProfile().getName(),
                member.getProfile().getEmail(),
                member.getProfile().getPhone(),
                member.getRole().name(),
                member.getStatus().name()
        );
    }
}
