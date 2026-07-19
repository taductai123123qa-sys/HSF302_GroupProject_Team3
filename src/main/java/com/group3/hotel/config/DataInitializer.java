package com.group3.hotel.config;

import com.group3.hotel.entity.Room;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.repository.RoomRepository;
import com.group3.hotel.entity.User;
import com.group3.hotel.entity.Customer;
import com.group3.hotel.enums.UserRole;
import com.group3.hotel.repository.UserRepository;
import com.group3.hotel.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Mới thêm

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            RoomCategoryRepository roomCategoryRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) { // Bơm bộ mã hóa vào đây
        return args -> {

            // 1. TẠO TÀI KHOẢN ADMIN (MỚI THÊM)
            if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
                User adminUser = User.builder()
                        .email("admin@gmail.com")
                        // Đã được mã hóa BCrypt
                        .password(passwordEncoder.encode("123456"))
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(adminUser);
                System.out.println("Tạo thành công tài khoản ADMIN: admin@gmail.com / 123456");
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

            // 3. DỮ LIỆU PHÒNG (GIỮ NGUYÊN NHƯ CŨ)
            if (roomCategoryRepository.count() == 0) {
                RoomCategory standard = RoomCategory.builder()
                        .name("Standard Room")
                        .pricePerNight(new BigDecimal("1000000.00"))
                        .description("A comfortable standard room with basic amenities.")
                        .capacity(2)
                        .size(25.0)
                        .imgUrl("/images/room/standard/standard_08.jpg")
                        .build();

                RoomCategory deluxe = RoomCategory.builder()
                        .name("Deluxe Room")
                        .pricePerNight(new BigDecimal("1500000.00"))
                        .description("A spacious deluxe room with premium amenities and a city view.")
                        .capacity(3)
                        .size(35.0)
                        .imgUrl("/images/room/deluxe/deluxe_08.jpg")
                        .build();

                RoomCategory suite = RoomCategory.builder()
                        .name("Suite")
                        .pricePerNight(new BigDecimal("2500000.00"))
                        .description("A luxurious suite with a separate living area and ocean view.")
                        .capacity(4)
                        .size(50.0)
                        .imgUrl("/images/room/suite/suite_08.jpg")
                        .build();

                roomCategoryRepository.saveAll(Arrays.asList(standard, deluxe, suite));

                Room room101 = Room.builder().roomNumber("101").roomStatus(RoomStatus.AVAILABLE).roomCategory(standard).build();
                Room room102 = Room.builder().roomNumber("102").roomStatus(RoomStatus.AVAILABLE).roomCategory(standard).build();
                Room room103 = Room.builder().roomNumber("103").roomStatus(RoomStatus.AVAILABLE).roomCategory(standard).build();

                Room room201 = Room.builder().roomNumber("201").roomStatus(RoomStatus.AVAILABLE).roomCategory(deluxe).build();
                Room room202 = Room.builder().roomNumber("202").roomStatus(RoomStatus.AVAILABLE).roomCategory(deluxe).build();

                Room room301 = Room.builder().roomNumber("301").roomStatus(RoomStatus.AVAILABLE).roomCategory(suite).build();

                roomRepository.saveAll(Arrays.asList(room101, room102, room103, room201, room202, room301));

                System.out.println("Sample Data Initialized!");
            }
        };
    }
}