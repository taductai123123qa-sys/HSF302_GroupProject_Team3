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
    RoomBooking createBooking(BookingCreateRequest request, Integer depositRate, BigDecimal totalPrice);

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
}
