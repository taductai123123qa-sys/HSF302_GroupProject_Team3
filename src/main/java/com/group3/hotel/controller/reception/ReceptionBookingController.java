package com.group3.hotel.controller.reception;

import com.group3.hotel.entity.BookingDetail;
import com.group3.hotel.entity.Room;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.service.IReceptionBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reception/bookings")
@RequiredArgsConstructor
public class ReceptionBookingController {

    private final IReceptionBookingService receptionBookingService;
    private final com.group3.hotel.service.IRoomService roomService;

    // Danh sách quản lý đơn đặt phòng
    @GetMapping
    public String listBookings(
            @RequestParam(name = "status", required = false) BookingStatus status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sortParam,
            Model model) {

        // Xử lý sort parameter
        String[] sortParts = sortParam.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

        Page<RoomBooking> bookingPage = receptionBookingService.getReceptionBookings(status, keyword, pageable);

        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortParam", sortParam);

        // Hiển thị danh sách các trạng thái
        model.addAttribute("allStatuses", BookingStatus.values());

        return "reception/booking-list";
    }

    // Chi tiết đơn
    @GetMapping("/{id}")
    public String bookingDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoomBooking booking = receptionBookingService.getBookingDetail(id);
            model.addAttribute("booking", booking);
            return "reception/booking-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/bookings";
        }
    }

    // Duyệt đơn
    @PostMapping("/{id}/approve")
    public String approveBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            receptionBookingService.approveBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn đặt phòng #" + id + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }

    // Từ chối/Hủy đơn
    @PostMapping("/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            receptionBookingService.rejectBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối/hủy đơn đặt phòng #" + id + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }

    // Màn hình check-in
    @GetMapping("/{id}/check-in")
    public String checkInView(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoomBooking booking = receptionBookingService.getBookingDetail(id);
            if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
                redirectAttributes.addFlashAttribute("error",
                        "Đơn hàng phải ở trạng thái CONFIRMED mới có thể check-in.");
                return "redirect:/reception/bookings/" + id;
            }
            model.addAttribute("booking", booking);

            // Lấy danh sách phòng trống theo từng hạng phòng
            java.util.Map<Long, java.util.List<Room>> availableRoomsMap = new java.util.HashMap<>();
            for (BookingDetail detail : booking.getBookingDetails()) {
                Long categoryId = detail.getRoomCategory().getId();
                if (!availableRoomsMap.containsKey(categoryId)) {
                    availableRoomsMap.put(categoryId, roomService.getAvailableRoomsByCategory(categoryId));
                }
            }
            model.addAttribute("availableRoomsMap", availableRoomsMap);

            return "reception/booking-checkin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/bookings";
        }
    }

    // Xử lý check-in
    @PostMapping("/{id}/check-in")
    public String processCheckIn(@PathVariable("id") Long id,
            @RequestParam java.util.Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin gán phòng từ request params: key có dạng "detailRoom[detailId]"
            // = "roomId"
            java.util.Map<Long, Long> detailRoomMap = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("detailRoom[")) {
                    String detailIdStr = entry.getKey().substring("detailRoom[".length(), entry.getKey().length() - 1);
                    Long detailId = Long.parseLong(detailIdStr);
                    Long roomId = Long.parseLong(entry.getValue());
                    detailRoomMap.put(detailId, roomId);
                }
            }

            receptionBookingService.checkInBooking(id, detailRoomMap);
            redirectAttributes.addFlashAttribute("success", "Đã Check-in và gán phòng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi Check-in: " + e.getMessage());
            return "redirect:/reception/bookings/" + id + "/check-in";
        }
        return "redirect:/reception/bookings/" + id;
    }

    // Xử lý check-out
    @PostMapping("/{id}/check-out")
    public String processCheckOut(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            receptionBookingService.checkOutBooking(id);
            redirectAttributes.addFlashAttribute("success",
                    "Đã check-out thành công. Các phòng đã chuyển sang trạng thái NEED_CLEANING.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }

    // Hiển thị đổi phòng
    @GetMapping("/{id}/change-room/{detailId}")
    public String changeRoomView(@PathVariable("id") Long id,
            @PathVariable("detailId") Long detailId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            RoomBooking booking = receptionBookingService.getBookingDetail(id);
            if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể đổi phòng khi khách đang CHECKED_IN.");
                return "redirect:/reception/bookings/" + id;
            }

            BookingDetail targetDetail = booking.getBookingDetails().stream()
                    .filter(d -> d.getId().equals(detailId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn."));

            model.addAttribute("booking", booking);
            model.addAttribute("detail", targetDetail);

            // Lấy tất cả category để có thể đổi khác hạng
            java.util.Map<Long, java.util.List<com.group3.hotel.entity.Room>> availableRoomsMap = new java.util.HashMap<>();
            java.util.List<com.group3.hotel.entity.RoomCategory> allCategories = roomService
                    .getAvailableRoomsByCategory(null).stream()
                    .map(com.group3.hotel.entity.Room::getRoomCategory)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            // Lấy tất cả phòng trống
            java.util.List<com.group3.hotel.entity.Room> allAvailableRooms = roomService
                    .getRoomsWithFilters(null, com.group3.hotel.enums.RoomStatus.AVAILABLE, null)
                    .stream()
                    .map(dto -> {
                        Room r = new com.group3.hotel.entity.Room();
                        r.setId(dto.getId());
                        r.setRoomNumber(dto.getRoomNumber());
                        com.group3.hotel.entity.RoomCategory cat = new com.group3.hotel.entity.RoomCategory();
                        cat.setId(dto.getRoomCategoryId());
                        cat.setName(dto.getRoomCategoryName());
                        // giả lập basePrice để hiển thị
                        r.setRoomCategory(cat);
                        return r;
                    }).collect(java.util.stream.Collectors.toList());

            model.addAttribute("allAvailableRooms", allAvailableRooms);

            return "reception/booking-change-room";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/bookings/" + id;
        }
    }

    // Xử lý đổi phòng
    @PostMapping("/{id}/change-room/{detailId}")
    public String processChangeRoom(@PathVariable("id") Long id,
            @PathVariable("detailId") Long detailId,
            @RequestParam("newRoomId") Long newRoomId,
            @RequestParam(value = "priceOption", defaultValue = "KEEP") String priceOption,
            RedirectAttributes redirectAttributes) {
        try {
            boolean keepPrice = "KEEP".equals(priceOption);
            receptionBookingService.changeRoom(id, detailId, newRoomId, keepPrice);
            redirectAttributes.addFlashAttribute("success", "Đã đổi phòng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đổi phòng: " + e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }
}
