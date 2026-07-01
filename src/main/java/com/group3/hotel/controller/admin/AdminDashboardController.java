package com.group3.hotel.controller.admin;

import com.group3.hotel.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"", "/", "/dashboard", "/statistics"})
    public String dashboard(Model model) {
        model.addAllAttributes(dashboardService.getDashboardData());
        return "admin/dashboard";
    }
}
