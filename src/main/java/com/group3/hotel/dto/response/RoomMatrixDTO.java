package com.group3.hotel.dto.response;

import com.group3.hotel.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMatrixDTO {
    private Long id;
    private String roomNumber;
    private RoomStatus roomStatus;
    private String roomCategoryName;
    private Long roomCategoryId;
    
    // Additional info when room is OCCUPIED
    private String currentGuestName;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
}
