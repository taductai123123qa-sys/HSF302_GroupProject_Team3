package com.group3.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class HomeController {

    @Autowired
    private com.group3.hotel.service.RoomCategoryService roomCategoryService;

    @Autowired
    private com.group3.hotel.service.HotelServiceService hotelServiceService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredRooms", roomCategoryService.getAllCategories().stream().limit(3).toList());
        model.addAttribute("featuredServices", hotelServiceService.getAllServices().stream().limit(2).toList());
        return "home/homepage";
    }
}
