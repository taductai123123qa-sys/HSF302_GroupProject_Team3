package com.group3.hotel.controller.customer;

import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.service.HotelServiceService;
import com.group3.hotel.service.RoomCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.security.Principal;

@Controller
@RequestMapping("/customer")
public class RoomController {

    @Autowired
    private RoomCategoryService roomCategoryService;

    @Autowired
    private HotelServiceService hotelServiceService;

    @Autowired
    private com.group3.hotel.service.IBookingService bookingService;

    @Autowired
    private com.group3.hotel.repository.CustomerRepository customerRepository;

    @GetMapping("/rooms")
    public String rooms(@PageableDefault(size = 6) Pageable pageable, Model model) {
        RoomSearchRequest searchRequest = new RoomSearchRequest();
        searchRequest.setCheckInDate(LocalDate.now());
        searchRequest.setCheckOutDate(LocalDate.now().plusDays(1));
        searchRequest.setCapacity(1);
        
        model.addAttribute("searchRequest", searchRequest);
        
        Page<RoomCategory> pagedCategories = roomCategoryService.searchCategory(searchRequest, pageable);
        
        model.addAttribute("roomCategories", pagedCategories.getContent());
        model.addAttribute("allRoomCategories", roomCategoryService.getAllCategories());
        model.addAttribute("currentPage", pagedCategories.getNumber() + 1);
        model.addAttribute("totalPages", pagedCategories.getTotalPages() > 0 ? pagedCategories.getTotalPages() : 1);
        
        return "customer/room-list";
    }

    @GetMapping("/rooms/search")
    public String search(@PageableDefault(size = 6) Pageable pageable, @ModelAttribute("searchRequest") RoomSearchRequest searchRequest, Model model){

        Page<RoomCategory> pagedCategories = roomCategoryService.searchCategory(searchRequest, pageable);

        model.addAttribute("roomCategories", pagedCategories.getContent());
        model.addAttribute("allRoomCategories", roomCategoryService.getAllCategories());
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("currentPage", pagedCategories.getNumber() + 1);
        model.addAttribute("totalPages", pagedCategories.getTotalPages() > 0 ? pagedCategories.getTotalPages() : 1);
        
        return "customer/room-list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable("id") Long id,
                             @ModelAttribute("searchRequest") RoomSearchRequest searchRequest,
                             @RequestParam(value = "roomCount", required = false) Integer roomCountParam,
                             Principal principal,
                             Model model) {
        RoomCategory category = roomCategoryService.getCategoryById(id);
        if (category == null) {
            return "redirect:/customer/rooms";
        }
        model.addAttribute("category", category);

        model.addAttribute("hotelServices", hotelServiceService.getAllServices());

        if (searchRequest.getCheckInDate() == null) {
            searchRequest.setCheckInDate(LocalDate.now());
        }
        if (searchRequest.getCheckOutDate() == null) {
            searchRequest.setCheckOutDate(LocalDate.now().plusDays(1));
        }
        model.addAttribute("searchRequest", searchRequest);
        
        int availableCount = roomCategoryService.getAvailableRoomCount(id, searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());
        category.setDynamicAvailableCount(Math.max(0, availableCount));

        BookingCreateRequest bookingRequest = new BookingCreateRequest();
        bookingRequest.setCategoryId(id);
        bookingRequest.setCheckInDate(searchRequest.getCheckInDate());
        bookingRequest.setCheckOutDate(searchRequest.getCheckOutDate());
        bookingRequest.setRoomCount((roomCountParam != null && roomCountParam > 0) ? roomCountParam : 1);
        if (principal != null) {
            customerRepository.findByUserEmail(principal.getName()).ifPresent(customer -> {
                bookingRequest.setFullName(customer.getFullName());
                bookingRequest.setPhone(customer.getPhone());
            });
        }
        
        model.addAttribute("bookingRequest", bookingRequest);

        long totalNights = ChronoUnit.DAYS.between(searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());
        if (totalNights <= 0) {
            totalNights = 1;
        }
        model.addAttribute("totalNights", totalNights);

        return "customer/room-detail";
    }

    @GetMapping("/booking/history")
    public String bookingHistory(
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        com.group3.hotel.dto.response.BookingHistoryDTO historyDTO = bookingService.getCustomerBookingHistory(email, status, sort);

        model.addAttribute("bookings", historyDTO.getBookings());
        model.addAttribute("countAll", historyDTO.getCountAll());
        model.addAttribute("countCheckedIn", historyDTO.getCountCheckedIn());
        model.addAttribute("countCancelled", historyDTO.getCountCancelled());

        model.addAttribute("currentStatus", status.toUpperCase());
        model.addAttribute("currentSort", sort.toLowerCase());

        return "customer/booking-history";
    }

    @org.springframework.web.bind.annotation.PostMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            bookingService.cancelCustomerBooking(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy phòng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/customer/booking/history";
    }

    @org.springframework.web.bind.annotation.PostMapping("/booking/request-change/{id}")
    public String requestRoomChange(@PathVariable Long id, @RequestParam("reason") String reason, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            bookingService.requestRoomChange(id, principal.getName(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu thành công! Lễ tân sẽ sớm liên hệ với bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/customer/booking/history";
    }
}
