package com.group3.hotel.controller.customer;

import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.entity.Payment;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.repository.PaymentRepository;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.service.IBookingService;
import com.group3.hotel.service.IVnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final RoomCategoryRepository roomCategoryRepository;
    private final IBookingService bookingService;
    private final IVnPayService vnPayService;

    private final PaymentRepository paymentRepository;

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

        // Tính tổng tiền gốc
        BigDecimal totalPrice = category.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.getRoomCount()));

        // Đẩy dữ liệu sang form chọn phương thức cọc
        model.addAttribute("bookingRequest", request);
        model.addAttribute("category", category);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("nights", nights);

        return "customer/booking-payment-select";
    }

    // 2. Endpoint xử lý tạo đơn tạm thời và chuyển hướng tới VNPay
    @PostMapping("/process-vnpay")
    public String processVnPay(
            @ModelAttribute BookingCreateRequest request,
            @RequestParam("depositRate") Integer depositRate, // Nhận tỷ lệ cọc 50 hoặc 100
            @RequestParam("totalPrice") BigDecimal totalPrice,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes,
            java.security.Principal principal) {

        String email = principal != null ? principal.getName() : "guest@hotel.com";

        // Uỷ thác nghiệp vụ lưu vào DB cho BookingService
        RoomBooking booking = bookingService.createBooking(request, depositRate, totalPrice, email);

        // Tính toán số tiền thanh toán thực tế dựa trên tỷ lệ cọc
        BigDecimal paymentAmountBd = totalPrice.multiply(BigDecimal.valueOf(depositRate)).divide(BigDecimal.valueOf(100));
        long paymentAmount = paymentAmountBd.longValue();

        // Lấy IP của user
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpRequest.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        // Uỷ thác việc gen URL cho VnPayService (Giả định VnPayService đã được update để xài txnRef)
        String paymentUrl = vnPayService.createPaymentUrl(booking.getId(), paymentAmount, ipAddress);
        
        // Redirect khách sang cổng thanh toán
        return "redirect:" + paymentUrl;
    }

    // 3. Endpoint nhận Callback/IPN trả về từ VNPAY
    @GetMapping("/vnpay-callback")
    public String vnpayCallback(@RequestParam Map<String, String> params, Model model) {
        
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        
        if (txnRef == null || txnRef.isEmpty()) {
            return "redirect:/";
        }

        // Cập nhật Gateway Transaction No vào bảng Payment
        Payment payment = paymentRepository.findByTxnRef(txnRef).orElse(null);
        if (payment != null && transactionNo != null && !transactionNo.isEmpty()) {
            payment.setGatewayTransactionNo(transactionNo);
            paymentRepository.save(payment);
        }

        // --- CƠ CHẾ KÍCH HOẠT BẮC CẦU IPN CHO MÔI TRƯỜNG DEV LOCALHOST ---
        try {
            Map<String, String> ipnParams = new HashMap<>(params);
            vnPayService.processIpn(ipnParams);
            System.out.println(">>> [LOCALHOST DEV] Đã kích hoạt IPN thành công cho Đơn hàng #" + txnRef);
        } catch (Exception e) {
            System.err.println(">>> [LOCALHOST DEV] Lỗi IPN: " + e.getMessage());
        }

        // Đẩy dữ liệu ra màn hình Kết quả thay vì Flash Attributes
        model.addAttribute("txnRef", txnRef);
        model.addAttribute("transactionNo", transactionNo);

        if ("00".equals(responseCode)) {
            model.addAttribute("status", "success");
            model.addAttribute("message", "Thanh toán thành công! Chúc bạn có một kỳ nghỉ tuyệt vời.");
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Giao dịch thanh toán thất bại hoặc đã bị hủy.");
        }
        
        return "customer/booking-result";
    }
}
