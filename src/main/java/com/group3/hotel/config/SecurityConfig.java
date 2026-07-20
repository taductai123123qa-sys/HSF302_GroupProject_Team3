package com.group3.hotel.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
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
                        .requestMatchers("/css/**", "/images/**", "/login", "/register").permitAll()
                        .anyRequest().authenticated()
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
                // Logout: cho phép cả GET và POST đều chạy được với URL /logout
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
