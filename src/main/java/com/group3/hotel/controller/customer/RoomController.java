package com.group3.hotel.controller.customer;

import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.service.HotelServiceService;
import com.group3.hotel.service.RoomCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import com.group3.hotel.repository.CustomerRepository;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.entity.Customer;
import com.group3.hotel.entity.RoomBooking;

@Controller
@RequestMapping("/customer")
public class RoomController {

    @Autowired
    private RoomCategoryService roomCategoryService;

    @Autowired
    private HotelServiceService hotelServiceService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoomBookingRepository roomBookingRepository;

    @GetMapping("/rooms")
    public String rooms(Model model) {
        RoomSearchRequest searchRequest = new RoomSearchRequest();
        // Đồng bộ ngày mặc định & số khách
        searchRequest.setCheckInDate(LocalDate.now());
        searchRequest.setCheckOutDate(LocalDate.now().plusDays(1));
        searchRequest.setCapacity(1);
        
        model.addAttribute("searchRequest", searchRequest);
        
        var allCategories = roomCategoryService.getAllCategories();
        model.addAttribute("roomCategories", allCategories);
        model.addAttribute("allRoomCategories", allCategories);
        
        return "customer/room-list";
    }

    @GetMapping("/rooms/search")
    public String search(@ModelAttribute("searchRequest") RoomSearchRequest searchRequest, Model model){

        model.addAttribute("roomCategories", roomCategoryService.searchCategory(searchRequest));
        model.addAttribute("allRoomCategories", roomCategoryService.getAllCategories());
        model.addAttribute("searchRequest", searchRequest);
        
        return "customer/room-list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable("id") Long id,
                             @ModelAttribute("searchRequest") RoomSearchRequest searchRequest,
                             @RequestParam(value = "roomCount", required = false) Integer roomCountParam,
                             Model model) {
        RoomCategory category = roomCategoryService.getCategoryById(id);
        if (category == null) {
            return "redirect:/customer/rooms";
        }
        model.addAttribute("category", category);

        // Lấy danh sách dịch vụ từ Service (Layered Architecture)
        model.addAttribute("hotelServices", hotelServiceService.getAllServices());

        // Đảm bảo không bị null ngày tháng
        if (searchRequest.getCheckInDate() == null) {
            searchRequest.setCheckInDate(LocalDate.now());
        }
        if (searchRequest.getCheckOutDate() == null) {
            searchRequest.setCheckOutDate(LocalDate.now().plusDays(1));
        }
        model.addAttribute("searchRequest", searchRequest);
        
        // Tính số lượng phòng trống thực tế
        int availableCount = roomCategoryService.getAvailableRoomCount(id, searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());
        category.setDynamicAvailableCount(Math.max(0, availableCount));

        // Khởi tạo BookingRequest
        BookingCreateRequest bookingRequest = new BookingCreateRequest();
        bookingRequest.setCategoryId(id);
        bookingRequest.setCheckInDate(searchRequest.getCheckInDate());
        bookingRequest.setCheckOutDate(searchRequest.getCheckOutDate());
        
        // Cấu hình linh hoạt số lượng phòng (từ request param, nếu ko có thì lấy mặc định là 1)
        bookingRequest.setRoomCount((roomCountParam != null && roomCountParam > 0) ? roomCountParam : 1);
        
        model.addAttribute("bookingRequest", bookingRequest);

        // Tính tổng số đêm
        long totalNights = ChronoUnit.DAYS.between(searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());
        if (totalNights <= 0) {
            totalNights = 1;
        }
        model.addAttribute("totalNights", totalNights);

        return "customer/room-detail";
    }

    @GetMapping("/booking/history")
    public String bookingHistory(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Customer customer = customerRepository.findByUserEmail(email).orElse(null);

        if (customer != null) {
            List<RoomBooking> bookings = roomBookingRepository.findByCustomerOrderByCreatedAtDesc(customer);
            model.addAttribute("bookings", bookings);
            
            long countAll = bookings.size();
            long countCheckedIn = bookings.stream().filter(b -> b.getBookingStatus().name().equals("CHECKED_IN")).count();
            long countCancelled = bookings.stream().filter(b -> b.getBookingStatus().name().equals("CANCELLED")).count();
            
            model.addAttribute("countAll", countAll);
            model.addAttribute("countCheckedIn", countCheckedIn);
            model.addAttribute("countCancelled", countCancelled);
        } else {
            model.addAttribute("bookings", java.util.Collections.emptyList());
        }

        return "customer/booking-history";
    }
}
