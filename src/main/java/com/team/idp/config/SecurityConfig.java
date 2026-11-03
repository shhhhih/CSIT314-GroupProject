package com.team.idp.config;

import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static final String USER_ADMIN = "USER_ADMIN";
    static final String INTERIOR_DESIGNER = "INTERIOR_DESIGNER";
    static final String CUSTOMER = "CUSTOMER";
    static final String PLATFORM_MANAGER = "PLATFORM_MANAGER";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterAfter(new SessionRoleFilter(), SecurityContextHolderFilter.class)
            .authorizeHttpRequests(auth -> auth
                
                // open to everyone
                .requestMatchers("/api/auth/**", "/api/hello").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/guest/idps").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/idps", "/api/idps/*").hasRole(CUSTOMER)
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/*").permitAll()


                // Interior Designer: own company, own IDPs
                .requestMatchers("/api/companies/me", "/api/companies/me/**").hasRole(INTERIOR_DESIGNER)
                .requestMatchers(HttpMethod.POST, "/api/companies").hasRole(INTERIOR_DESIGNER)
                .requestMatchers(HttpMethod.GET, "/api/companies/*").permitAll()   
                .requestMatchers("/api/designer/**").hasRole(INTERIOR_DESIGNER)

                // Customer (member only)
                .requestMatchers("/api/favourites", "/api/favourites/**").hasRole(CUSTOMER)

                // Platform Manager: categories (create / update / delete) and reports
                .requestMatchers("/api/categories", "/api/categories/**").hasRole(PLATFORM_MANAGER)
                .requestMatchers("/api/reports/**").hasRole(PLATFORM_MANAGER)

                // User Admin: user profiles
                .requestMatchers("/api/admin/**").hasRole(USER_ADMIN)

                // any logged-in role: own profile and anything else under /api
                .requestMatchers("/api/**").authenticated()

                // static pages, css, js
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) ->
                    writeJson(response, 401, "Please log in first"))
                .accessDeniedHandler((request, response, e) ->
                    writeJson(response, 403, "You do not have permission to do this"))
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeJson(jakarta.servlet.http.HttpServletResponse response, int status, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}