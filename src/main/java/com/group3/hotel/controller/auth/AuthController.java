package com.group3.hotel.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        // Trả về file giao diện login.html nằm trong thư mục templates/auth
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        // Trả về file giao diện register.html (bạn có thể tạo file này sau)
        return "auth/register";
    }
}