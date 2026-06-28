package com.group3.hotel.service.impl;

import com.group3.hotel.entity.BookingDetail;
import com.group3.hotel.entity.Room;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.BookingDetailRepository;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.repository.RoomRepository;
import com.group3.hotel.service.RoomAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomAllocationServiceImpl implements RoomAllocationService {

    private final RoomRepository roomRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RoomBookingRepository roomBookingRepository;

    @Override
    @Transactional
    public void assignTemporaryRooms(RoomBooking booking, RoomCategory category, int roomCount) {
        // Lấy danh sách các phòng trống của loại phòng này
        List<Room> availableRooms = roomRepository.findByRoomCategoryIdAndRoomStatus(category.getId(), RoomStatus.AVAILABLE);

        if (availableRooms.size() < roomCount) {
            throw new IllegalArgumentException("Không đủ phòng trống để gán tạm thời.");
        }

        // Bốc đủ số lượng phòng khách đặt
        for (int i = 0; i < roomCount; i++) {
            Room room = availableRooms.get(i);
            
            // Đổi trạng thái phòng thành đang giữ chỗ (OCCUPIED) để khách khác không đặt được
            room.setRoomStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);

            // Tạo chi tiết đơn hàng lưu lại lịch sử
            BookingDetail detail = BookingDetail.builder()
                    .roomBooking(booking)
                    .roomCategory(category)
                    .room(room)
                    .price(category.getPricePerNight())
                    .build();
            bookingDetailRepository.save(detail);
        }
        log.info("Đã gán tạm thời {} phòng cho đơn hàng ID: {}", roomCount, booking.getId());
    }

    @Override
    @Transactional
    public void releaseRoomsForCancelledBooking(RoomBooking booking) {
        List<BookingDetail> details = bookingDetailRepository.findByRoomBookingId(booking.getId());
        
        for (BookingDetail detail : details) {
            Room room = detail.getRoom();
            if (room != null) {
                // Trả lại trạng thái phòng trống cho hệ thống
                room.setRoomStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
            }
        }
        log.info("Đã giải phóng phòng vật lý cho đơn hàng bị hủy/thất bại ID: {}", booking.getId());
    }
}
