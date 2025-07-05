package com.example.attendanceTracker.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class RoleConfig {

    private final UserRepository userRepo;

    public RoleConfig(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    
    @Bean
    public Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            String email = jwt.getClaimAsString("email");
            User user = userRepo.findByEmail(email)
               .orElseThrow(() ->
                new OAuth2AuthenticationException(
                    new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        "Không tìm thấy user với email: " + email,
                        null
                    )
                )
            );
            String role = user.getRole().name(); // ví dụ "ADMIN"
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            return new JwtAuthenticationToken(jwt, List.of(authority), jwt.getSubject());
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return new AccessDeniedHandler() {
            private final ObjectMapper mapper = new ObjectMapper();

            @Override
            public void handle(HttpServletRequest request,
                               HttpServletResponse response,
                               org.springframework.security.access.AccessDeniedException accessDeniedException)
                    throws IOException, ServletException {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json;charset=UTF-8");

                Map<String, Object> body = Map.of(
                    "error", "Forbidden",
                    "message", "Bạn không có quyền vào tài nguyên này",
                    "status", HttpStatus.FORBIDDEN.value(),
                    "path", request.getRequestURI()
                );
                mapper.writeValue(response.getWriter(), body);
            }
        };
    }
}
