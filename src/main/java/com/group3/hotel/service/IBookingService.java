package com.group3.hotel.service;

import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.entity.RoomBooking;

import java.math.BigDecimal;

public interface IBookingService {

    /**
     * Tạo một đơn đặt phòng nháp (PENDING) và lưu vào Database.
     * Đồng thời gán tạm thời các phòng vật lý và tạo đối tượng Payment trạng thái PENDING.
     *
     * @param request Form chứa thông tin người dùng nhập trên giao diện
     * @param depositRate Tỷ lệ tiền cọc (50 hoặc 100)
     * @param totalPrice Tổng tiền gốc (chưa nhân tỷ lệ)
     * @return Đối tượng RoomBooking vừa tạo
     */
    RoomBooking createBooking(BookingCreateRequest request, Integer depositRate, BigDecimal totalPrice, String email);

    /**
     * Xác nhận đơn đặt phòng thành công (CONFIRMED).
     * @param bookingId Mã đơn
     */
    void confirmBooking(Long bookingId);

    /**
     * Hủy đơn đặt phòng và giải phóng phòng tạm thời.
     * @param bookingId Mã đơn
     */
    void cancelBooking(Long bookingId);

    /**
     * Hủy đơn từ phía khách hàng (có kiểm tra quyền)
     */
    void cancelCustomerBooking(Long bookingId, String email) throws Exception;

    /**
     * Yêu cầu đổi phòng hoặc nâng hạng
     */
    void requestRoomChange(Long bookingId, String email, String reason) throws Exception;

    /**
     * Lấy lịch sử đặt phòng của khách hàng
     */
    com.group3.hotel.dto.response.BookingHistoryDTO getCustomerBookingHistory(String email, String status, String sort);

    /**
     * Tính toán tổng tiền và số đêm
     */
    com.group3.hotel.dto.response.BookingSummaryDTO calculateBookingSummary(BookingCreateRequest request, com.group3.hotel.entity.RoomCategory category);

    /**
     * Cập nhật mã giao dịch từ VNPAY
     */
    void updatePaymentGatewayTransaction(String txnRef, String transactionNo);
}
