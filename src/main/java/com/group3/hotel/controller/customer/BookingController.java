package com.group3.hotel.controller.customer;

import com.group3.hotel.config.VNPayConfig;
import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.entity.User;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.repository.PaymentRepository;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.repository.UserRepository;
import com.group3.hotel.service.RoomAllocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final RoomCategoryRepository roomCategoryRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final VNPayConfig vnPayConfig;
    private final RoomAllocationService roomAllocationService;

    // 1. Endpoint xử lý form đặt phòng từ trang Detail và chuyển qua giao diện chọn mức thanh toán (50% hoặc 100%)
    @PostMapping("/select-payment")
    public String selectPaymentMethod(@ModelAttribute BookingCreateRequest request, Model model) {
        RoomCategory category = roomCategoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category == null) {
            return "redirect:/"; // Lỗi không tìm thấy loại phòng
        }

        // Tính số đêm lưu trú (ít nhất 1 đêm)
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) nights = 1;

        // Tính tổng tiền gốc (Chưa tính dịch vụ thêm, chỉ tính tiền phòng * số đêm * số lượng phòng)
        BigDecimal totalPrice = category.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.getRoomCount()));

        // Đẩy dữ liệu sang form chọn phương thức cọc
        model.addAttribute("bookingRequest", request);
        model.addAttribute("category", category);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("nights", nights);

        // Trả về view để khách hàng chọn thanh toán 50% hoặc 100%
        return "customer/booking-payment-select";
    }

    // 2. Endpoint xử lý tạo đơn tạm thời và chuyển hướng tới VNPay
    @PostMapping("/process-vnpay")
    public String processVnPay(
            @ModelAttribute BookingCreateRequest request,
            @RequestParam("depositRate") Integer depositRate, // Nhận tỷ lệ cọc 50 hoặc 100
            @RequestParam("totalPrice") BigDecimal totalPrice,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        // Giả lập lấy User đang đăng nhập (Thực tế nên lấy từ SecurityContext)
        User currentUser = userRepository.findById(1L).orElse(null); 
        // Nếu không có, bạn cần tạo một User Anonymous hoặc bắt đăng nhập.

        // Tính toán số tiền thanh toán thực tế dựa trên tỷ lệ cọc (50% hoặc 100%)
        BigDecimal paymentAmountBd = totalPrice.multiply(BigDecimal.valueOf(depositRate)).divide(BigDecimal.valueOf(100));
        long paymentAmount = paymentAmountBd.longValue();

        // Cơ chế phòng vệ dữ liệu (Data Defense) chống lỗi 500
        Integer finalGuestCount = (request.getGuestCount() != null && request.getGuestCount() > 0) ? request.getGuestCount() : 1;
        LocalDate finalCheckIn = request.getCheckInDate() != null ? request.getCheckInDate() : LocalDate.now();
        LocalDate finalCheckOut = request.getCheckOutDate() != null ? request.getCheckOutDate() : LocalDate.now().plusDays(1);

        // Lưu đơn đặt phòng nháp (Trạng thái PENDING) xuống Database để lấy mã đơn (ID)
        RoomBooking booking = RoomBooking.builder()
                .user(currentUser)
                .checkInDate(finalCheckIn)
                .checkOutDate(finalCheckOut)
                .totalPrice(totalPrice)
                .bookingStatus(BookingStatus.PENDING) // Sử dụng PENDING có sẵn thay vì PENDING_PAYMENT
                .numberOfGuests(finalGuestCount)
                .notes(request.getNotes())
                .build();
        
        booking = roomBookingRepository.save(booking);

        // [Logic mới] Bốc phòng vật lý trống và gán tạm thời cho khách (Ghi vào BookingDetail)
        RoomCategory category = roomCategoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category != null) {
            roomAllocationService.assignTemporaryRooms(booking, category, request.getRoomCount());
        }

        // Lưu trạng thái thanh toán (PaymentStatus.PENDING) vào bảng Payment
        com.group3.hotel.entity.Payment payment = com.group3.hotel.entity.Payment.builder()
                .roomBooking(booking)
                .amount(paymentAmountBd)
                .paymentMethod(com.group3.hotel.enums.PaymentMethod.VNPAY)
                .paymentDate(LocalDateTime.now())
                .status(com.group3.hotel.enums.PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Chuẩn bị các tham số cho VNPAY dựa theo code cũ
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(booking.getId()); // Mã đơn hàng (ID Booking)
        String vnp_IpAddr = vnPayConfig.getIpAddress(httpRequest);
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(paymentAmount * 100)); // VNPAY nhận số tiền x100 (VD: 100000 VND -> 10000000)
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan dat phong " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Set thời gian tạo giao dịch và thời gian hết hạn (Expire time) 15 phút
        LocalDateTime now = LocalDateTime.now();
        vnp_Params.put("vnp_CreateDate", vnPayConfig.formatExpireDate(now));
        vnp_Params.put("vnp_ExpireDate", vnPayConfig.formatExpireDate(now.plusMinutes(15)));

        // Ký chữ ký số bằng thuật toán HMAC-SHA512 để đảm bảo bảo mật
        String secureHash = vnPayConfig.hashAllFields(vnp_Params);
        vnp_Params.put("vnp_SecureHash", secureHash);

        // Xây dựng URL cuối cùng để Redirect khách sang cổng thanh toán VNPAY
        StringBuilder queryUrl = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                if (queryUrl.length() > 0) {
                    queryUrl.append('&');
                }
                queryUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String paymentUrl = vnPayConfig.getVnp_Url() + "?" + queryUrl.toString();
        
        // Redirect khách sang cổng thanh toán
        return "redirect:" + paymentUrl;
    }

    // 3. Endpoint nhận Callback/IPN trả về từ VNPAY
    @GetMapping("/vnpay-callback")
    public String vnpayCallback(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        
        if (txnRef == null || txnRef.isEmpty()) {
            return "redirect:/";
        }

        Long bookingId = Long.parseLong(txnRef);
        RoomBooking booking = roomBookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return "redirect:/";
        }

        com.group3.hotel.entity.Payment payment = paymentRepository.findFirstByRoomBookingIdOrderByIdDesc(bookingId).orElse(null);

        if ("00".equals(responseCode)) {
            // Giao dịch thành công
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            roomBookingRepository.save(booking);

            if (payment != null) {
                payment.setStatus(com.group3.hotel.enums.PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công! Chúc bạn có một kỳ nghỉ tuyệt vời.");
            // Tạm thời redirect về trang chủ hoặc trang lịch sử (thay bằng link thực tế của bạn)
            return "redirect:/"; 
        } else {
            // Giao dịch thất bại hoặc bị hủy bởi khách
            booking.setBookingStatus(BookingStatus.CANCELLED);
            roomBookingRepository.save(booking);

            if (payment != null) {
                payment.setStatus(com.group3.hotel.enums.PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }

            // [Logic Giải phóng phòng] Ngay lập tức trả phòng về trạng thái AVAILABLE
            roomAllocationService.releaseRoomsForCancelledBooking(booking);

            redirectAttributes.addFlashAttribute("errorMessage", "Giao dịch thanh toán thất bại hoặc đã bị hủy.");
            return "redirect:/";
        }
    }
}
