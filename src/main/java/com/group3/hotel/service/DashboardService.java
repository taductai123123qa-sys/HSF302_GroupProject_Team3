package com.group3.hotel.service;

import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.HotelServiceRepository;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final HotelServiceRepository hotelServiceRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new LinkedHashMap<>();

        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.countByRoomStatus(RoomStatus.AVAILABLE);
        long occupiedRooms = roomRepository.countByRoomStatus(RoomStatus.OCCUPIED);
        long maintenanceRooms = roomRepository.countByRoomStatus(RoomStatus.MAINTENANCE);
        long totalServices = hotelServiceRepository.count();

        BigDecimal occupancyRate = BigDecimal.ZERO;
        if (totalRooms > 0) {
            occupancyRate = BigDecimal.valueOf(occupiedRooms * 100.0 / totalRooms)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        List<RoomBooking> bookings = roomBookingRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long checkedOutBookings = 0;

        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();

        for (RoomBooking booking : bookings) {
            if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
                checkedOutBookings++;
                if (booking.getTotalPrice() != null) {
                    totalRevenue = totalRevenue.add(booking.getTotalPrice());

                    YearMonth month = YearMonth.from(booking.getCheckOutDate());
                    String key = month.toString();
                    BigDecimal oldValue = revenueByMonth.getOrDefault(key, BigDecimal.ZERO);
                    revenueByMonth.put(key, oldValue.add(booking.getTotalPrice()));
                }
            }
        }

        data.put("totalRooms", totalRooms);
        data.put("availableRooms", availableRooms);
        data.put("occupiedRooms", occupiedRooms);
        data.put("maintenanceRooms", maintenanceRooms);
        data.put("totalServices", totalServices);
        data.put("totalBookings", bookings.size());
        data.put("checkedOutBookings", checkedOutBookings);
        data.put("totalRevenue", totalRevenue);
        data.put("occupancyRate", occupancyRate);
        data.put("revenueByMonth", revenueByMonth);

        return data;
    }
}
