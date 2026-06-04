package com.app.obsession.member.domain;

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
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
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