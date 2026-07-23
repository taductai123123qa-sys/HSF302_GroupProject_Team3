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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reception/bookings")
@RequiredArgsConstructor
public class ReceptionBookingController {

    private final IReceptionBookingService receptionBookingService;

    // 1. Danh sách quản lý đơn đặt phòng
    @GetMapping
    public String listBookings(
            @RequestParam(name = "status", required = false) BookingStatus status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sortParam,
            Model model) {

        Pageable pageable = createPageable(page, size, sortParam);
        Page<RoomBooking> bookingPage = receptionBookingService.getReceptionBookings(status, keyword, pageable);

        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortParam", sortParam);
        model.addAttribute("allStatuses", BookingStatus.values());

        return "reception/booking-list";
    }

    // 2. Chi tiết đơn
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

    // 3. Duyệt đơn
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

    // 4. Từ chối/Hủy đơn
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

    // 5. Màn hình check-in
    @GetMapping("/{id}/check-in")
    public String checkInView(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoomBooking booking = receptionBookingService.getBookingDetail(id);
            if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
                redirectAttributes.addFlashAttribute("error", "Đơn hàng phải ở trạng thái CONFIRMED mới có thể check-in.");
                return "redirect:/reception/bookings/" + id;
            }

            Map<Long, List<Room>> availableRoomsMap = receptionBookingService.getAvailableRoomsByCategoryForBooking(booking);

            model.addAttribute("booking", booking);
            model.addAttribute("availableRoomsMap", availableRoomsMap);

            return "reception/booking-checkin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/bookings";
        }
    }

    // 6. Xử lý check-in
    @PostMapping("/{id}/check-in")
    public String processCheckIn(@PathVariable("id") Long id,
                                 @RequestParam Map<String, String> allParams,
                                 RedirectAttributes redirectAttributes) {
        try {
            Map<Long, Long> detailRoomMap = parseDetailRoomMap(allParams);
            receptionBookingService.checkInBooking(id, detailRoomMap);
            redirectAttributes.addFlashAttribute("success", "Đã Check-in và gán phòng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi Check-in: " + e.getMessage());
            return "redirect:/reception/bookings/" + id + "/check-in";
        }
        return "redirect:/reception/bookings/" + id;
    }

    // 7. Xử lý check-out
    @PostMapping("/{id}/check-out")
    public String processCheckOut(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            receptionBookingService.checkOutBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã check-out thành công. Các phòng đã chuyển sang trạng thái NEED_CLEANING.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/bookings/" + id;
    }

    // 8. Màn hình đổi phòng
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

            List<Room> allAvailableRooms = receptionBookingService.getAllAvailableRooms();

            model.addAttribute("booking", booking);
            model.addAttribute("detail", targetDetail);
            model.addAttribute("allAvailableRooms", allAvailableRooms);

            return "reception/booking-change-room";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/bookings/" + id;
        }
    }

    // 9. Xử lý đổi phòng
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

     // Tạo đối tượng Pageable từ thông số trang, kích thước và tham số sắp xếp.
    private Pageable createPageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(Math.max(0, page - 1), size, Sort.by(direction, sortField));
    }

    /**
     * Tách thông tin gán phòng từ request parameters (dạng key: detailRoom[detailId] = roomId).
     */
    private Map<Long, Long> parseDetailRoomMap(Map<String, String> allParams) {
        Map<Long, Long> detailRoomMap = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("detailRoom[")) {
                String detailIdStr = entry.getKey().substring("detailRoom[".length(), entry.getKey().length() - 1);
                Long detailId = Long.parseLong(detailIdStr);
                Long roomId = Long.parseLong(entry.getValue());
                detailRoomMap.put(detailId, roomId);
            }
        }
        return detailRoomMap;
    }
}
