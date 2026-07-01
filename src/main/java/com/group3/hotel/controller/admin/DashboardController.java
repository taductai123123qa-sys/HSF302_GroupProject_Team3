package com.group3.hotel.controller.admin;

import com.group3.hotel.repository.BookingDetailRepository;
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

    @GetMapping
    public String showDashboard(Model model) {
        List<Object[]> revenueData = bookingDetailRepository.getRevenueByCategory();
        List<String> categoryLabels = new ArrayList<>();
        List<Double> categoryRevenue = new ArrayList<>();

        for (Object[] row : revenueData) {
            categoryLabels.add((String) row[0]);
            categoryRevenue.add(row[1] != null ? ((BigDecimal) row[1]).doubleValue() : 0.0);
        }

        List<Object[]> countData = bookingDetailRepository.getBookingCountByCategory();
        List<Integer> occupancyRates = new ArrayList<>();

        for (Object[] row : countData) {
            occupancyRates.add(row[1] != null ? ((Long) row[1]).intValue() : 0);
        }

        model.addAttribute("categoryLabels", categoryLabels.isEmpty() ? List.of("Chưa có dữ liệu") : categoryLabels);
        model.addAttribute("categoryRevenue", categoryRevenue.isEmpty() ? List.of(0.0) : categoryRevenue);
        model.addAttribute("occupancyRates", occupancyRates.isEmpty() ? List.of(0) : occupancyRates);

        return "admin/dashboard";
    }
}