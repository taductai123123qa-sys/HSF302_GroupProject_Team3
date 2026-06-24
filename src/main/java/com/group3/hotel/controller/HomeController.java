package com.group3.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.group3.hotel.dto.request.RoomSearchRequest;
import java.util.ArrayList;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "home/homepage";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("searchRequest", new RoomSearchRequest());
        model.addAttribute("roomCategories", new ArrayList<>());
        model.addAttribute("allRoomCategories", new ArrayList<>());
        return "customer/room-list";
    }
}
