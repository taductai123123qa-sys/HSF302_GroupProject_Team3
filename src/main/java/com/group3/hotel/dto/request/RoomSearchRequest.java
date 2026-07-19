package com.group3.hotel.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchRequest {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate = LocalDate.now();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate = LocalDate.now().plusDays(1);

    private Integer capacity = 2;
    private Long roomCategory;
    private Double minPrice;
    private Double maxPrice;
    private Integer numberOfNights = 1;
}
