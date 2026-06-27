package com.group3.hotel.config;

import com.group3.hotel.entity.Room;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoomCategoryRepository roomCategoryRepository, RoomRepository roomRepository) {
        return args -> {
            if (roomCategoryRepository.count() == 0) {
                // Create Room Categories
                RoomCategory standard = RoomCategory.builder()
                        .name("Standard Room")
                        .pricePerNight(new BigDecimal("100.00"))
                        .description("A comfortable standard room with basic amenities.")
                        .capacity(2)
                        .size(25.0)
                        .imgUrl("/images/room/standard/standard_08.jpg")
                        .build();

                RoomCategory deluxe = RoomCategory.builder()
                        .name("Deluxe Room")
                        .pricePerNight(new BigDecimal("150.00"))
                        .description("A spacious deluxe room with premium amenities and a city view.")
                        .capacity(3)
                        .size(35.0)
                        .imgUrl("/images/room/deluxe/deluxe_08.jpg")
                        .build();

                RoomCategory suite = RoomCategory.builder()
                        .name("Suite")
                        .pricePerNight(new BigDecimal("250.00"))
                        .description("A luxurious suite with a separate living area and ocean view.")
                        .capacity(4)
                        .size(50.0)
                        .imgUrl("/images/room/suite/suite_08.jpg")
                        .build();

                roomCategoryRepository.saveAll(Arrays.asList(standard, deluxe, suite));

                // Create Rooms
                Room room101 = Room.builder().roomNumber("101").roomStatus(RoomStatus.AVAILABLE).roomCategory(standard).build();
                Room room102 = Room.builder().roomNumber("102").roomStatus(RoomStatus.AVAILABLE).roomCategory(standard).build();
                Room room103 = Room.builder().roomNumber("103").roomStatus(RoomStatus.OCCUPIED).roomCategory(standard).build();

                Room room201 = Room.builder().roomNumber("201").roomStatus(RoomStatus.AVAILABLE).roomCategory(deluxe).build();
                Room room202 = Room.builder().roomNumber("202").roomStatus(RoomStatus.NEED_CLEANING).roomCategory(deluxe).build();

                Room room301 = Room.builder().roomNumber("301").roomStatus(RoomStatus.AVAILABLE).roomCategory(suite).build();

                roomRepository.saveAll(Arrays.asList(room101, room102, room103, room201, room202, room301));
                
                System.out.println("Sample Data Initialized!");
            } else {
                // Update existing categories if they still have the old placeholder URLs
                var categories = roomCategoryRepository.findAll();
                boolean updated = false;
                for (RoomCategory cat : categories) {
                    if (cat.getImgUrl() != null && cat.getImgUrl().contains("example.com")) {
                        if (cat.getName().contains("Standard")) {
                            cat.setImgUrl("/images/room/standard/standard_08.jpg");
                        } else if (cat.getName().contains("Deluxe")) {
                            cat.setImgUrl("/images/room/deluxe/deluxe_08.jpg");
                        } else if (cat.getName().contains("Suite")) {
                            cat.setImgUrl("/images/room/suite/suite_08.jpg");
                        } else {
                            cat.setImgUrl("/images/room/standard/standard_08.jpg");
                        }
                        updated = true;
                    }
                }
                if (updated) {
                    roomCategoryRepository.saveAll(categories);
                    System.out.println("Sample Data Updated with Local Image URLs!");
                }
            }
        };
    }
}
