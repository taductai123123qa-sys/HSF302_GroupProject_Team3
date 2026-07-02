package com.group3.hotel.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    @GetMapping
    public String showStatistics(Model model) {
        // Gửi dữ liệu giả để màn hình Thống kê hiện lên đẹp đẽ, không lỗi 500
        model.addAttribute("totalRooms", 15);
        model.addAttribute("availableRooms", 8);
        model.addAttribute("occupiedRooms", 7);
        model.addAttribute("occupancyRate", 46);
        model.addAttribute("totalRevenue", 125000000);
        return "admin/statistics";
    }
}