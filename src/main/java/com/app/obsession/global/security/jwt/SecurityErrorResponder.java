package com.app.obsession.global.security.jwt;

import com.app.obsession.global.exception.ErrorCode;
import com.app.obsession.global.response.ErrorResponse;
import com.app.obsession.global.security.exception.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

    private static final String PROBLEM_BASE_URI = "about:blank/";

    private final JsonMapper jsonMapper;

    public void unauthorized(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        write(request, response, AuthErrorCode.UNAUTHORIZED);
    }

    public void accessDenied(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        write(request, response, AuthErrorCode.ACCESS_DENIED);
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode code
    ) throws IOException {
        ErrorResponse body = ErrorResponse.problem(
                problemType(code.code()),
                code.code(),
                code.status(),
                code.message(),
                request.getRequestURI(),
                code.code(),
                null,
                null
        );

        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getWriter(), body);
    }

    private String problemType(String title) {
        return PROBLEM_BASE_URI + title.toLowerCase().replace('_', '-');
    }
}
