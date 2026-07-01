package com.group3.hotel.controller;

import com.group3.hotel.entity.Room;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.service.AdminRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final AdminRoomService adminRoomService;
    private final RoomCategoryRepository roomCategoryRepository;

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", adminRoomService.getAllRooms());
        return "admin/rooms";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("room", Room.builder().roomStatus(RoomStatus.AVAILABLE).floor(1).build());
        addFormData(model);
        return "admin/room-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("room", adminRoomService.getRoomById(id));
        addFormData(model);
        return "admin/room-form";
    }

    @PostMapping("/save")
    public String saveRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        adminRoomService.saveRoom(room);
        redirectAttributes.addFlashAttribute("success", "Lưu phòng thành công");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminRoomService.deleteRoom(id);
        redirectAttributes.addFlashAttribute("success", "Xóa phòng thành công");
        return "redirect:/admin/rooms";
    }

    private void addFormData(Model model) {
        model.addAttribute("categories", roomCategoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("statuses", RoomStatus.values());
    }
}
