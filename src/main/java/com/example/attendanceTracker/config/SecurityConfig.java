package com.example.attendanceTracker.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final Converter<Jwt, JwtAuthenticationToken> jwtAuthConverter;

    public SecurityConfig(Converter<Jwt, JwtAuthenticationToken> jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Dùng issuer URI từ JWT claims của bạn
        String issuerUri = "https://dev-x1wbvdvsvedd7o0r.us.auth0.com/";
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    // @Bean
    // JwtAuthenticationConverter jwtAuthenticationConverter() {
    //     JwtGrantedAuthoritiesConverter rolesConverter = new JwtGrantedAuthoritiesConverter();
    //     rolesConverter.setAuthorityPrefix("ROLE_");
    //     rolesConverter.setAuthoritiesClaimName("https://attendance.com/roles");

    //     JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    //     converter.setJwtGrantedAuthoritiesConverter(rolesConverter);
    //     return converter;
    // }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .cors(Customizer.withDefaults())
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(HttpMethod.OPTIONS).permitAll()
              .anyRequest().authenticated()
          )
          .oauth2ResourceServer(oauth2 -> oauth2
              .jwt(jwt -> jwt
                  .jwtAuthenticationConverter(jwtAuthConverter)
              )
          );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
