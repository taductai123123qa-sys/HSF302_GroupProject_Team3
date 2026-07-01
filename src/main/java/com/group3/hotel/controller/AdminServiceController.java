package com.group3.hotel.controller;

import com.group3.hotel.entity.HotelService;
import com.group3.hotel.enums.ServiceStatus;
import com.group3.hotel.enums.ServiceType;
import com.group3.hotel.service.AdminHotelServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final AdminHotelServiceService adminHotelServiceService;

    @GetMapping
    public String listServices(Model model) {
        model.addAttribute("services", adminHotelServiceService.getAllServices());
        return "admin/services";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("hotelService", HotelService.builder()
                .serviceType(ServiceType.OTHER)
                .status(ServiceStatus.ACTIVE)
                .build());
        addFormData(model);
        return "admin/service-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("hotelService", adminHotelServiceService.getServiceById(id));
        addFormData(model);
        return "admin/service-form";
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute HotelService hotelService, RedirectAttributes redirectAttributes) {
        adminHotelServiceService.saveService(hotelService);
        redirectAttributes.addFlashAttribute("success", "Lưu dịch vụ thành công");
        return "redirect:/admin/services";
    }

    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminHotelServiceService.deleteService(id);
        redirectAttributes.addFlashAttribute("success", "Xóa dịch vụ thành công");
        return "redirect:/admin/services";
    }

    private void addFormData(Model model) {
        model.addAttribute("types", ServiceType.values());
        model.addAttribute("statuses", ServiceStatus.values());
    }
}
