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
                .antMatchers("/user/cart/update").permitAll()
                .antMatchers("/user/cart/place-order").permitAll()
                .antMatchers("/user/order-summary/**").permitAll()
                .antMatchers("/user/orders/active").permitAll()
                .antMatchers(  "/user/orders/orderhistory").permitAll()
                .antMatchers(  "/user/update-user").permitAll()
                .antMatchers(  "/user/delete/{uuid}").permitAll()

                .antMatchers("/merchant/create").permitAll()  // Allow registration
                .antMatchers("/merchant/login").permitAll()  // Allow login
                .antMatchers("/merchant/add-item").permitAll()  // Allow additem
                .antMatchers("/merchant/get-food-items").permitAll()  // Allow get item
                .antMatchers("/merchant/update-food-item").permitAll()// Allow update item
                .antMatchers("/merchant/orders/active").permitAll()// Allow update item
                .antMatchers( "/merchant/orders/mark-as-ready").permitAll()
                .antMatchers( "/merchant/orders/ready").permitAll()
                .antMatchers( "/merchant/orders/mark-as-done").permitAll()
                .antMatchers( "/merchant/orders/history").permitAll()
                .antMatchers( "/merchant/sales/today").permitAll()
                .antMatchers( "/merchant/update-food-availability").permitAll()
                .antMatchers("/restaurants").permitAll()
                .antMatchers("/restaurants/{uuid}").permitAll()// Allow get restaurant
                .antMatchers("/api/upload-image").permitAll()
                .antMatchers( "/merchant/delete/{uuid}").permitAll()

                .antMatchers("/admin/login").permitAll()
                .antMatchers("/admin/register").permitAll()
                .antMatchers("/admin/user-list").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: " + authException.getMessage());
                })
                .and()
                .headers().contentTypeOptions().disable();
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
