package com.team.idp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionRoleFilter extends OncePerRequestFilter {

    static String toRole(String profileName) {
        return profileName.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Object userId = session.getAttribute("userId");
            Object profileName = session.getAttribute("profileName");
            if (userId instanceof Long && profileName instanceof String name && !name.isBlank()) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + toRole(name)))));
                SecurityContextHolder.setContext(context);
            }
        }
        chain.doFilter(request, response);
    }
}