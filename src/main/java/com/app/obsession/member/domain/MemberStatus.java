package com.app.obsession.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    WITHDRAWN("탈퇴");

    private final String description;
}