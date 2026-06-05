package com.app.obsession.member.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.member.application.LoginService;
import com.app.obsession.member.application.SignupService;
import com.app.obsession.member.presentation.dto.LoginRequest;
import com.app.obsession.member.presentation.dto.LoginResponse;
import com.app.obsession.member.presentation.dto.SignupRequest;
import com.app.obsession.member.presentation.dto.SignupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final SignupService signupService;
    private final LoginService loginService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public CommonResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        Long memberId = signupService.signup(request.toCommand());

        return CommonResponse.created(
                "회원가입: 일반 회원가입에 성공했습니다.",
                SignupResponse.of(memberId));
    }

    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = loginService.login(request.toCommand());

        return CommonResponse.success(
                "로그인: 일반 로그인에 성공했습니다.",
                response
        );
    }

}

