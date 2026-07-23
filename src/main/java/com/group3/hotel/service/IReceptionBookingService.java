package com.group3.hotel.service;

import com.group3.hotel.entity.Room;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

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

    /**
     * Lễ tân thực hiện Check-in (CONFIRMED -> CHECKED_IN)
     * Đồng thời gán các phòng thực tế (Room) vào các chi tiết đơn (BookingDetail)
     * @param bookingId Mã đơn
     * @param detailRoomMap Map chứa {BookingDetailId : RoomId} do Lễ tân chọn
     */
    void checkInBooking(Long bookingId, Map<Long, Long> detailRoomMap);

    /**
     * Lễ tân thực hiện Check-out (CHECKED_IN -> CHECKED_OUT)
     * Đổi trạng thái các phòng thành NEED_CLEANING
     * @param bookingId Mã đơn
     */
    void checkOutBooking(Long bookingId);

    /**
     * Đổi phòng cho khách đã check-in
     * @param bookingId Mã đơn
     * @param detailId Mã chi tiết đơn
     * @param newRoomId Mã phòng mới
     * @param keepPrice Giữ nguyên giá phòng cũ hay tính theo giá mới
     */
    void changeRoom(Long bookingId, Long detailId, Long newRoomId, boolean keepPrice);

    /**
     * Lấy danh sách phòng trống theo từng loại phòng của đơn đặt (phục vụ Check-in)
     * @param booking Đơn đặt phòng
     * @return Map chứa key là categoryId và value là danh sách Room trống
     */
    Map<Long, List<Room>> getAvailableRoomsByCategoryForBooking(RoomBooking booking);

    /**
     * Lấy tất cả phòng đang ở trạng thái AVAILABLE (phục vụ Đổi phòng)
     * @return List of Room
     */
    List<Room> getAllAvailableRooms();
}

