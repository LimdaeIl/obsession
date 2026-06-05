package com.app.obsession.member.presentation.controller;

import com.app.obsession.global.response.CommonResponse;
import com.app.obsession.global.security.auth.CustomUserDetails;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/members")
@RestController
public class MemberController {

    @GetMapping("/me")
    public CommonResponse<Map<String, Object>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.success(Map.of(
                "memberId", userDetails.getMemberId(),
                "email", userDetails.getEmail(),
                "role", userDetails.getRole()
        ));
    }
}
