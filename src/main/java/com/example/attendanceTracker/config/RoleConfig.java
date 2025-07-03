package com.example.attendanceTracker.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.UserRepository;

@Configuration
public class RoleConfig {

    private UserRepository userRepo;

    public RoleConfig(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Bean
    public Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            String email = jwt.getClaimAsString("email");
            User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy user với email: " + email
                    )
                );
            String role = user.getRole().name(); // ví dụ "ADMIN"
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            return new JwtAuthenticationToken(jwt, List.of(authority), jwt.getSubject());
        };
    }
}
