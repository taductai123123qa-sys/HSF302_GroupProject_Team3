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
}
