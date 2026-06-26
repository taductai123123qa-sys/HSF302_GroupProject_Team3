package com.group3.hotel.controller.customer;

import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.service.RoomCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class RoomController {

    @Autowired
    private RoomCategoryService roomCategoryService;


    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("searchRequest", new RoomSearchRequest());
        
        var allCategories = roomCategoryService.getAllCategories();
        model.addAttribute("roomCategories", allCategories);
        model.addAttribute("allRoomCategories", allCategories);
        
        return "customer/room-list";
    }


    @GetMapping("/rooms/search")
    public String search(@ModelAttribute("searchRequest") RoomSearchRequest searchRequest, Model model){

        model.addAttribute("roomCategories", roomCategoryService.searchCategory(searchRequest));
        model.addAttribute("allRoomCategories", roomCategoryService.getAllCategories());
        model.addAttribute("searchRequest",searchRequest);
        
        return "customer/room-list";
    }


}
