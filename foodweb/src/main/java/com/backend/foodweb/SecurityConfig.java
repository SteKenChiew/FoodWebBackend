package com.backend.foodweb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtUtils jwtUtils;

    public SecurityConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and() // Enable CORS
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/user/create").permitAll()  // Allow registration
                .antMatchers("/user/login").permitAll() // Allow login
                .antMatchers("/user/cart").permitAll()
                .antMatchers("/user/cart/add").permitAll()// Allow login
                .antMatchers("/user/cart/items").permitAll()
                .antMatchers("/merchant/create").permitAll()  // Allow registration
                .antMatchers("/merchant/login").permitAll()  // Allow login
                .antMatchers("/merchant/add-item").permitAll()  // Allow additem
                .antMatchers("/merchant/get-food-items").permitAll()  // Allow get item
                .antMatchers("/merchant/update-food-item").permitAll()// Allow update item
                .antMatchers("/restaurants").permitAll()
                .antMatchers("/restaurants/{uuid}").permitAll()// Allow get restaurant
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: " + authException.getMessage());
                });
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
