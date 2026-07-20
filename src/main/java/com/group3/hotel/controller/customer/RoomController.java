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
    private com.group3.hotel.service.ICustomerService customerService;

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
            customerService.findByUserEmail(principal.getName()).ifPresent(customer -> {
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

}
