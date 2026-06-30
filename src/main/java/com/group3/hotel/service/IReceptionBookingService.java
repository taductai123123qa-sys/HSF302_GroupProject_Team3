package com.group3.hotel.service;

import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReceptionBookingService {
    
    /**
     * Lấy danh sách Booking dành cho Lễ tân, có phân trang, tìm kiếm và lọc trạng thái.
     * @param status Trạng thái đơn (tuỳ chọn)
     * @param keyword Từ khoá (ID, tên khách, SĐT - tuỳ chọn)
     * @param pageable Phân trang
     * @return Page of RoomBooking
     */
    Page<RoomBooking> getReceptionBookings(BookingStatus status, String keyword, Pageable pageable);

    /**
     * Lấy chi tiết đơn đặt phòng cho Lễ tân (Không giới hạn quyền sở hữu)
     * @param bookingId Mã đơn
     * @return RoomBooking
     */
    RoomBooking getBookingDetail(Long bookingId);

    /**
     * Lễ tân duyệt đơn đặt phòng (PENDING -> CONFIRMED)
     * @param bookingId Mã đơn
     */
    void approveBooking(Long bookingId);

    /**
     * Lễ tân từ chối đơn đặt phòng (PENDING/CONFIRMED -> CANCELLED)
     * @param bookingId Mã đơn
     */
    void rejectBooking(Long bookingId);
}
