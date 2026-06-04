package com.app.obsession.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {
    CUSTOMER("사용자"),
    BUSINESS("사업자"),
    ADMIN("관리자");

    private final String description;

}
