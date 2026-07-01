package com.group3.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Khai báo bộ mã hóa mật khẩu.
    // Spring sẽ tự động kết hợp cái này với CustomUserDetailsService của bạn.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình giao diện và phân quyền
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tạm tắt CSRF để dễ test
                .authorizeHttpRequests(auth -> auth
                        // Mở cửa tự do cho giao diện login và các file ảnh, css
                        .requestMatchers("/css/**", "/images/**", "/login", "/register").permitAll()
                        // Mọi request khác đều phải đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Báo cho Spring biết: "Hãy dùng giao diện login của tôi!"
                        .usernameParameter("email") // Trùng với thuộc tính name="email" trong file HTML
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true) // Đăng nhập thành công về trang chủ
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                );

        return http.build();
    }
}