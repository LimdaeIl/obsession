package com.app.obsession.member.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import com.app.obsession.member.application.LoginService;
import com.app.obsession.member.application.LogoutService;
import com.app.obsession.member.application.ReissueTokenService;
import com.app.obsession.member.application.SignupService;
import com.app.obsession.member.presentation.dto.LoginRequest;
import com.app.obsession.member.presentation.dto.LoginResponse;
import com.app.obsession.member.presentation.dto.ReissueRequest;
import com.app.obsession.member.presentation.dto.SignupRequest;
import com.app.obsession.member.presentation.dto.SignupResponse;
import com.app.obsession.member.presentation.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final SignupService signupService;
    private final LoginService loginService;
    private final ReissueTokenService reissueTokenService;
    private final LogoutService logoutService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public CommonResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        Long memberId = signupService.signup(request.toCommand());

        return CommonResponse.created(
                "회원가입: 일반 회원가입에 성공했습니다.",
                SignupResponse.of(memberId)
        );
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

    @PostMapping("/reissue")
    public CommonResponse<TokenResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        TokenResponse response = reissueTokenService.reissue(request.refreshToken());

        return CommonResponse.success(
                "토큰: Access Token 재발급에 성공했습니다.",
                response
        );
    }

    @PostMapping("/logout")
    public CommonResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Authorization") String authorization
    ) {
        logoutService.logout(userDetails.getMemberId(), authorization);

        return CommonResponse.success(
                "로그아웃: 로그아웃에 성공했습니다.",
                null
        );
    }
}
