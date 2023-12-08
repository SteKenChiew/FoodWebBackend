package com.backend.foodweb;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            Authentication auth = createAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // Logic to extract JWT token from the request headers or cookies
        // Example: Authorization: Bearer <token>
        // Implement according to your application's token extraction mechanism

        // For example, you can extract the token from the Authorization header like this:
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }

    private Authentication createAuthentication(String token) {
        // Extract user information from the token and create an Authentication object
        // Example: Claims claims = jwtUtils.decodeToken(token);
        //          String username = claims.getSubject();
        //          Set<GrantedAuthority> authorities = extractAuthorities(claims);
        //          return new UsernamePasswordAuthenticationToken(username, null, authorities)

        // Implement the logic to extract user information from the token and create Authentication
        // Return a new UsernamePasswordAuthenticationToken with the extracted details

        // For example:
        // Claims claims = jwtUtils.decodeToken(token);
        // String username = claims.getSubject();
        // Set<GrantedAuthority> authorities = extractAuthorities(claims);
        // return new UsernamePasswordAuthenticationToken(username, null, authorities);

        return null; // Replace this line with your actual implementation
    }
}
