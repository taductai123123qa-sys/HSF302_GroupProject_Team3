package com.group3.hotel.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/images/**", "/js/**", "/", "/login", "/register", "/debug/me", "/error", "/403").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reception/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers("/customer/**", "/auth/profile").hasAnyRole("ADMIN", "GUEST")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
                        })
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().contains("ADMIN"));
                            boolean isReceptionist = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().contains("RECEPTIONIST"));

                            if (isAdmin) {
                                response.sendRedirect("/admin/dashboard");
                            } else if (isReceptionist) {
                                response.sendRedirect("/reception/bookings");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(request -> {
                            String uri = request.getRequestURI();
                            return uri != null && uri.endsWith("/logout");
                        })
                        .addLogoutHandler((HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
                            if (req.getSession() != null) {
                                req.getSession().invalidate();
                            }
                            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", "");
                            cookie.setMaxAge(0);
                            cookie.setPath("/");
                            res.addCookie(cookie);
                        })
                        .logoutSuccessHandler((HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
                            res.sendRedirect(req.getContextPath() + "/login?logout");
                        })
                        .permitAll()
                );

        return http.build();
    }
}