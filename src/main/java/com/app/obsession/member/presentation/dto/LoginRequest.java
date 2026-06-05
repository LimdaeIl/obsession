package com.app.obsession.member.presentation.dto;

import com.app.obsession.member.application.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "로그인: 이메일은 필수입니다.")
        String email,

        @NotBlank(message = "로그인: 비밀번호는 필수입니다.")
        String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
