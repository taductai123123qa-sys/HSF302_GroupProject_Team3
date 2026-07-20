package com.group3.hotel.config;


import com.group3.hotel.entity.User;
import com.group3.hotel.entity.Customer;
import com.group3.hotel.enums.UserRole;
import com.group3.hotel.repository.UserRepository;
import com.group3.hotel.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Mới thêm



@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) { // Bơm bộ mã hóa vào đây
        return args -> {

            // 1. TẠO TÀI KHOẢN ADMIN (MỚI THÊM)
            // Cập nhật password nếu user đã tồn tại nhưng password chưa đúng chuẩn BCrypt
            java.util.Optional<User> adminOpt = userRepository.findByEmail("admin@gmail.com");
            if (adminOpt.isEmpty()) {
                User adminUser = User.builder()
                        .email("admin@gmail.com")
                        // Đã được mã hóa BCrypt — mật khẩu thật là 123456
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(adminUser);
                System.out.println("Tạo thành công tài khoản ADMIN: admin@gmail.com / 123456");
            } else {
                User adminUser = adminOpt.get();
                String adminHash = passwordEncoder.encode("123456");
                adminUser.setPassword(adminHash);
                userRepository.save(adminUser);
                System.out.println("Đã cập nhật lại password BCrypt cho ADMIN: admin@gmail.com / 123456");
            }

            if (userRepository.findByEmail("receptionist@gmail.com").isEmpty()) {
                User receptionistUser = User.builder()
                        .email("receptionist@gmail.com")
                        // Đã được mã hóa BCrypt
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.RECEPTIONIST)
                        .build();
                userRepository.save(receptionistUser);
                System.out.println("Tạo thành công tài khoản RECEPTIONIST: receptionist@gmail.com / 123456");
            }

            // 2. KHỞI TẠO TÀI KHOẢN GUEST MỚI (ĐÃ FIX MÃ HÓA)
            // Đổi email một chút thành guest2 để code chạy lệnh tạo mới
            if (userRepository.findByEmail("guest2@hotel.com").isEmpty()) {
                User guestUser = User.builder()
                        .email("guest2@hotel.com")
                        // Đã được mã hóa BCrypt
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.GUEST)
                        .build();
                userRepository.save(guestUser);

                Customer guestCustomer = Customer.builder()
                        .user(guestUser)
                        .fullName("Guest User 2")
                        .phone("0123456789")
                        .build();
                customerRepository.save(guestCustomer);
                System.out.println("Tạo thành công tài khoản GUEST: guest2@hotel.com / 123456");
            }

            // 2.5 TẠO TÀI KHOẢN LỄ TÂN (HOẶC CẬP NHẬT MẬT KHẨU MÃ HÓA NẾU CHƯA CÓ)
            java.util.Optional<User> letanOpt = userRepository.findByEmail("letan@gmail.com");
            if (letanOpt.isEmpty()) {
                User letan = User.builder()
                        .email("letan@gmail.com")
                        .password(passwordEncoder.encode("12345"))
                        .role(UserRole.RECEPTIONIST)
                        .build();
                userRepository.save(letan);
                System.out.println("Tạo thành công tài khoản LỄ TÂN: letan@gmail.com / 12345");
            } else {
                User letan = letanOpt.get();
                // Nếu password không bắt đầu bằng $2a$ (định dạng của BCrypt) thì cập nhật lại
                if (!letan.getPassword().startsWith("$2a$")) {
                    letan.setPassword(passwordEncoder.encode("12345"));
                    userRepository.save(letan);
                    System.out.println("Cập nhật mật khẩu mã hóa cho tài khoản LỄ TÂN: letan@gmail.com / 12345");
                }
            }

            // 3. DỮ LIỆU PHÒNG ĐÃ BỊ XÓA THEO YÊU CẦU CỦA USER
            System.out.println("Sample Data Initialized (Accounts only)!");
        };
    }
}