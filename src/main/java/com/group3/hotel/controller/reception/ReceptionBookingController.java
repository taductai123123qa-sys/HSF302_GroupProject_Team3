package com.group3.hotel.controller.reception;

import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.service.IReceptionBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reception/bookings")
@RequiredArgsConstructor
public class ReceptionBookingController {

    private final IReceptionBookingService receptionBookingService;

    // 1. Màn 8: Danh sách quản lý đơn đặt phòng
    @GetMapping
    public String listBookings(
            @RequestParam(name = "status", required = false) BookingStatus status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sortParam,
            Model model) {

        // Xử lý sort parameter (e.g., "checkInDate,asc")
        String[] sortParts = sortParam.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
        
        Page<RoomBooking> bookingPage = receptionBookingService.getReceptionBookings(status, keyword, pageable);

        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortParam", sortParam);
        
        // Pass list of statuses for the filter dropdown
        model.addAttribute("allStatuses", BookingStatus.values());

        return "reception/booking-list";
    }

    // 2. Màn hình Chi tiết đơn
    @GetMapping("/{id}")
    public String bookingDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoomBooking booking = receptionBookingService.getBookingDetail(id);
            model.addAttribute("booking", booking);
            return "reception/booking-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/bookings";
        }
    }

    // 3. API Duyệt đơn (Sử dụng form method override để mapping PUT)
    @PutMapping("/{id}/approve")
    public String approveBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            receptionBookingService.approveBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn đặt phòng #" + id + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }

    // 4. API Từ chối/Hủy đơn
    @PutMapping("/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            receptionBookingService.rejectBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối/hủy đơn đặt phòng #" + id + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }
}
