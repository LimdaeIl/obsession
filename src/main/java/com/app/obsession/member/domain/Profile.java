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
public class Profile {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 11)
    private String phone;

    public Profile(String name, String email, String phone) {
        validate(name, email, phone);
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Profile change(String name, String phone) {
        return new Profile(name, this.email, phone);
    }

    private void validate(String name, String email, String phone) {
        if (name == null || name.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_NAME);
        }

        if (email == null || email.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_EMAIL);
        }

        if (phone == null || phone.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_PHONE);
        }
    }
}

