package com.app.obsession.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.obsession.global.security.exception.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class SecurityErrorResponderTest {

    private SecurityErrorResponder securityErrorResponder;

    @BeforeEach
    void setUp() {
        securityErrorResponder = new SecurityErrorResponder(new JsonMapper());
    }

    @Test
    @DisplayName("UNAUTHORIZED 응답을 ErrorResponse 포맷으로 작성한다")
    void unauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/api/v1/members/me");

        securityErrorResponder.unauthorized(request, response);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        String content = response.getContentAsString();

        assertThat(content).contains("\"status\":401");
        assertThat(content).contains("\"title\":\"UNAUTHORIZED\"");
        assertThat(content).contains("\"errorCode\":\"UNAUTHORIZED\"");
        assertThat(content).contains("\"detail\":\"인증: 인증이 필요합니다.\"");
        assertThat(content).contains("\"instance\":\"/api/v1/members/me\"");
    }

    @Test
    @DisplayName("ACCESS_DENIED 응답을 ErrorResponse 포맷으로 작성한다")
    void accessDenied() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/api/v1/admin/test");

        securityErrorResponder.accessDenied(request, response);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        String content = response.getContentAsString();

        assertThat(content).contains("\"status\":403");
        assertThat(content).contains("\"title\":\"ACCESS_DENIED\"");
        assertThat(content).contains("\"errorCode\":\"ACCESS_DENIED\"");
        assertThat(content).contains("\"detail\":\"인증: 접근 권한이 없습니다.\"");
        assertThat(content).contains("\"instance\":\"/api/v1/admin/test\"");
    }

    @Test
    @DisplayName("지정한 AuthErrorCode로 응답을 작성한다")
    void write_customAuthErrorCode() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/api/v1/members/me");

        securityErrorResponder.write(
                request,
                response,
                AuthErrorCode.BLACKLISTED_ACCESS_TOKEN
        );

        assertThat(response.getStatus()).isEqualTo(401);

        String content = response.getContentAsString();

        assertThat(content).contains("\"title\":\"BLACKLISTED_ACCESS_TOKEN\"");
        assertThat(content).contains("\"errorCode\":\"BLACKLISTED_ACCESS_TOKEN\"");
        assertThat(content).contains("\"detail\":\"인증: 로그아웃 처리된 Access Token입니다.\"");
    }
}
