package com.app.obsession.global.security.jwt;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionHandlingFilterTest {

    @Mock
    private SecurityErrorResponder securityErrorResponder;

    @Mock
    private FilterChain filterChain;

    private SecurityExceptionHandlingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityExceptionHandlingFilter(securityErrorResponder);
    }

    @Test
    @DisplayName("하위 필터에서 AuthException이 발생하면 SecurityErrorResponder가 응답을 작성한다")
    void doFilter_authException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthException exception = new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);

        doThrow(exception)
                .when(filterChain)
                .doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(securityErrorResponder).write(
                request,
                response,
                AuthErrorCode.INVALID_ACCESS_TOKEN
        );
    }

    @Test
    @DisplayName("하위 필터에서 예외가 없으면 SecurityErrorResponder를 호출하지 않는다")
    void doFilter_noException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(securityErrorResponder, never()).write(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
