package com.group3.hotel.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @GetMapping
    public String showDashboard(Model model) {
        // 1. Dữ liệu: Doanh thu theo hạng phòng
        List<String> categoryLabels = Arrays.asList("Standard Room", "Deluxe Room", "Suite");
        List<Double> categoryRevenue = Arrays.asList(15000000.0, 28000000.0, 12000000.0);

        // 2. Dữ liệu: Tỷ lệ lấp đầy phòng (%)
        List<Integer> occupancyRates = Arrays.asList(85, 60, 40);

        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryRevenue", categoryRevenue);
        model.addAttribute("occupancyRates", occupancyRates);

        return "admin/dashboard";
    }
}