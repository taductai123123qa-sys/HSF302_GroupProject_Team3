package com.group3.hotel.controller.auth;

import com.group3.hotel.entity.Customer;
import com.group3.hotel.entity.User;
import com.group3.hotel.enums.UserRole;
import com.group3.hotel.repository.CustomerRepository;
import com.group3.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/auth/profile")
    public String showProfile(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);

                if (user.getRole() == UserRole.GUEST) {
                    Optional<Customer> customerOpt = customerRepository.findByUserEmail(email);
                    customerOpt.ifPresent(customer -> model.addAttribute("customer", customer));
                }
            }
        }
        return "auth/profile";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("fullName") String fullName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email này đã được đăng ký!");
            return "auth/register";
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(UserRole.GUEST);
        userRepository.save(newUser);

        Customer newCustomer = new Customer();
        newCustomer.setFullName(fullName);
        newCustomer.setPhone(phone);
        newCustomer.setUser(newUser);

        customerRepository.save(newCustomer);

        redirectAttributes.addFlashAttribute("success", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}