package com.example.AttendanceTracker.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Import(RoleConfig.class)  
public class SecurityConfig {

    private final Converter<Jwt, JwtAuthenticationToken> jwtAuthConverter;

    private final AccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(
      Converter<Jwt, JwtAuthenticationToken> jwtAuthConverter,
      AccessDeniedHandler customAccessDeniedHandler
    ) {
        this.jwtAuthConverter = jwtAuthConverter;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = "https://dev-x1wbvdvsvedd7o0r.us.auth0.com/";
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .cors(Customizer.withDefaults())
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(HttpMethod.OPTIONS).permitAll()
              .anyRequest().authenticated()
          )
            .exceptionHandling(ex -> ex
              .accessDeniedHandler(customAccessDeniedHandler)
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
