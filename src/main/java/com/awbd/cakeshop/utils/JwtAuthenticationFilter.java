package com.awbd.cakeshop.utils;

import com.awbd.cakeshop.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String token = extractBearer(authHeader);

        if (token != null) {
            try {
                if (tokenBlacklistService.esteInvalid(token)) {
                    log.info("JWT este în blacklist. Continui neautentificat. path={}", request.getRequestURI());
                    SecurityContextHolder.clearContext();
                } else if (jwtUtil.validateToken(token)) {
                    final String username = jwtUtil.getUsernameFromToken(token);
                    final User.Role role = jwtUtil.getRoleFromToken(token);

                    if (username != null && role != null) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("JWT valid: user={}, role={}, path={}", username, role, request.getRequestURI());
                    } else {
                        log.warn("JWT valid structural dar fără subiect/rol. Continui neautentificat. path={}", request.getRequestURI());
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    log.info("JWT invalid sau expirat. Continui neautentificat. path={}", request.getRequestURI());
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                log.warn("Eroare la procesarea JWT: {}. Continui neautentificat. path={}", e.getMessage(), request.getRequestURI());
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String extractBearer(String header) {
        if (header == null) return null;
        if (header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String t = header.substring(7).trim();
            if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
                t = t.substring(1, t.length() - 1).trim();
            }
            return t.isEmpty() ? null : t;
        }
        return null;
    }
}
