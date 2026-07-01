package com.group3.hotel.controller.reception;

import com.group3.hotel.entity.Room;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.service.IRoomService;
import com.group3.hotel.service.RoomCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reception/rooms")
@RequiredArgsConstructor
public class ReceptionRoomController {

    private final IRoomService roomService;
    private final RoomCategoryService roomCategoryService;

    // 1. Xem sơ đồ phòng (Room Matrix)
    @GetMapping
    public String roomMatrix(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        List<com.group3.hotel.dto.response.RoomMatrixDTO> rooms = roomService.getRoomsWithFilters(keyword, status, categoryId);
        
        // Group rooms by category for matrix display
        Map<String, List<com.group3.hotel.dto.response.RoomMatrixDTO>> roomsByCategory = rooms.stream()
                .collect(Collectors.groupingBy(com.group3.hotel.dto.response.RoomMatrixDTO::getRoomCategoryName, java.util.TreeMap::new, Collectors.toList()));

        model.addAttribute("roomsByCategory", roomsByCategory);
        model.addAttribute("categories", roomCategoryService.getAllCategories());
        model.addAttribute("allStatuses", RoomStatus.values());
        
        // Retain filters
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);

        return "reception/room-matrix";
    }

    // 2. Cập nhật trạng thái phòng (VD: dọn xong)
    @PostMapping("/{id}/status")
    public String updateRoomStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") RoomStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            roomService.updateRoomStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái phòng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/reception/rooms";
    }
}
