package com.github.jaguililla.appointments.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.oauth2.jwt.JwtDecoders.fromIssuerLocation;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    private static final String[] PERMITTED_ENDPOINTS = {
        "/actuator/info",
        "/actuator/info/**",
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/metrics",
        "/actuator/metrics/**",
        "/v*/api-docs/**",
        "/v*/api-docs*",
        "/swagger-ui.html",
        "/swagger-ui/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        // TODO There should be a way to inject the JwtDecoder (this is a Spring property)
        @Value("${spring.security.oauth2.resourceserver.jwk.issuer-uri}") String issuer
    ) throws Exception {

        var jwtDecoder = fromIssuerLocation(issuer);

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(PERMITTED_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)))
            .build();
    }
}
