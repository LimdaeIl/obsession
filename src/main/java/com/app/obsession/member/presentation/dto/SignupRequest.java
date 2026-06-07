package com.app.obsession.member.presentation.dto;

import com.app.obsession.member.application.command.SignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "회원가입: 이메일은 필수입니다.")
        @Email(message = "회원가입: 이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "회원가입: 이메일은 최대 100자까지 입력할 수 있습니다.")
        String email,

        @NotBlank(message = "회원가입: 비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "회원가입: 비밀번호는 8자 이상 30자 이하로 입력해야 합니다.")
        String password,

        @NotBlank(message = "회원가입: 이름은 필수입니다.")
        @Size(max = 50, message = "회원가입: 이름은 최대 50자까지 입력할 수 있습니다.")
        String name,

        @NotBlank(message = "회원가입: 전화번호는 필수입니다.")
        @Pattern(regexp = "^\\d{10,11}$", message = "회원가입: 전화번호는 숫자 10~11자리여야 합니다.")
        String phone
) {

    public SignupCommand toCommand() {
        return new SignupCommand(email, password, name, phone);
    }
}

