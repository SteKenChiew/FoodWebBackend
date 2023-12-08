package com.backend.foodweb;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public Key key() {
        // Use Keys.secretKeyFor to create a key with sufficient size
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration}")
    private long expiration;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(key(), issuer, expiration);
    }
}
