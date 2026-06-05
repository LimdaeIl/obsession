package com.app.obsession.member.domain;

import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Password {

    @Column(name = "password", length = 255)
    private String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password encoded(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_PASSWORD);
        }
        return new Password(encodedPassword);
    }

    public static Password empty() {
        return new Password(null);
    }

    public boolean exists() {
        return value != null && !value.isBlank();
    }
}
