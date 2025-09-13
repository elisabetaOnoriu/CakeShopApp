package com.awbd.cakeshop.security;

import com.awbd.cakeshop.utils.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ---- Pagini publice (Thymeleaf/HTML) ----
                        .requestMatchers("/", "/home", "/welcome", "/login", "/register").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/cake/**",      // ex. /cake/1 (pagina)
                                "/cakes/**",     // ex. /cakes/1 (alternativ)
                                "/categories/**",
                                "/chefs/**",
                                "/cart", "/cart/**"
                        ).permitAll()

                        // ---- Resurse statice ----
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ---- Endpoints de autentificare ----
                        .requestMatchers("/api/users/login", "/api/users/register", "/api/auth/**").permitAll()

                        // ---- Read-only public API ----
                        .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/cakes/**").permitAll()

                        // ---- Reviews: public GET, POST necesită autentificare ----
                        // !!! IMPORTANT: Aceste reguli TREBUIE înainte de blocul ADMIN pe /api/cakes/**
                        .requestMatchers(HttpMethod.GET,  "/api/cakes/*/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cakes/*/reviews/**").authenticated()
                        // (opțional, dacă ai și fallback /api/reviews)
                        .requestMatchers(HttpMethod.GET,  "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()

                        // ---- Cart: user logat ----
                        .requestMatchers("/api/cart/**").authenticated()

                        // ---- Write pe cakes/categories: doar ADMIN (lasă-le DUPĂ excepția pentru reviews) ----
                        .requestMatchers(HttpMethod.POST,   "/api/categories/**", "/api/cakes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/categories/**", "/api/cakes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/cakes/**").hasRole("ADMIN")

                        // restul cererilor
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
