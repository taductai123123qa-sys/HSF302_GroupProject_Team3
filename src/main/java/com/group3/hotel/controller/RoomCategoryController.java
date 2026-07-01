package com.group3.hotel.controller;

import com.group3.hotel.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/categories") // Đây là "địa chỉ" để truy cập
public class RoomCategoryController {

    @Autowired
    private RoomCategoryRepository categoryRepository;

    @GetMapping
    public String listCategories(Model model) {
        // Lấy danh sách từ DB và truyền sang giao diện HTML
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/category-list"; // Trỏ đến file HTML tên category-list.html
    }
}