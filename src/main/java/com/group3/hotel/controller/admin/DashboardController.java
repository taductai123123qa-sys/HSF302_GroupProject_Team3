package com.group3.hotel.controller.admin;

import com.group3.hotel.repository.BookingDetailRepository;
import com.group3.hotel.repository.RoomRepository;
import com.group3.hotel.repository.HotelServiceRepository;
import com.group3.hotel.enums.RoomStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private HotelServiceRepository hotelServiceRepository;

    @GetMapping
    public String showDashboard(Model model) {
        // 1. Lấy dữ liệu thật từ DB cho 4 thẻ
        model.addAttribute("totalRooms", roomRepository.count());
        model.addAttribute("availableRooms", roomRepository.countByRoomStatus(RoomStatus.AVAILABLE));
        model.addAttribute("totalServices", hotelServiceRepository.count());

        // 2. Lấy dữ liệu doanh thu (Cộng dồn từ query bạn đã có)
        List<Object[]> revenueData = bookingDetailRepository.getRevenueByCategory();
        double totalRevenue = 0;
        List<String> categoryLabels = new ArrayList<>();
        List<Double> categoryRevenue = new ArrayList<>();

        for (Object[] row : revenueData) {
            categoryLabels.add((String) row[0]);
            double rev = row[1] != null ? ((BigDecimal) row[1]).doubleValue() : 0.0;
            categoryRevenue.add(rev);
            totalRevenue += rev;
        }
        model.addAttribute("totalRevenue", totalRevenue);

        // 3. Data cho biểu đồ
        List<Object[]> countData = bookingDetailRepository.getBookingCountByCategory();
        List<Integer> occupancyRates = new ArrayList<>();
        for (Object[] row : countData) {
            occupancyRates.add(row[1] != null ? ((Long) row[1]).intValue() : 0);
        }

        model.addAttribute("categoryLabels", categoryLabels.isEmpty() ? List.of("Trống") : categoryLabels);
        model.addAttribute("categoryRevenue", categoryRevenue.isEmpty() ? List.of(0.0) : categoryRevenue);
        model.addAttribute("occupancyRates", occupancyRates.isEmpty() ? List.of(0) : occupancyRates);

        return "admin/dashboard";
    }
}