package com.example.attendanceTracker.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper(); // để trả JSON

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorBody = new HashMap<>();

        if (authException instanceof OAuth2AuthenticationException oae) {
            String description = oae.getError().getDescription();

            if (description != null && description.contains("Không tìm thấy user")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                errorBody.put("error", description);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                errorBody.put("error", description != null ? description : "Unauthorized");
            }

        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // fallback 401
            errorBody.put("error", "Unauthorized");
        }

        // Ghi JSON ra body
        String responseJson = objectMapper.writeValueAsString(errorBody);
        response.getWriter().write(responseJson);
    }
}
