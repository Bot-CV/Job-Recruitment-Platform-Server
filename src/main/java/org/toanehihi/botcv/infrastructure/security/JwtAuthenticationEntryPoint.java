package org.toanehihi.botcv.infrastructure.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        DataResponse<Void> dataResponse = DataResponse.<Void>builder()
                .code(ErrorCode.AUTH_UNAUTHORIZED.getCode())
                .message(ErrorCode.AUTH_UNAUTHORIZED.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        response.getWriter().write(objectMapper.writeValueAsString(dataResponse));
        response.flushBuffer();
    }
}
