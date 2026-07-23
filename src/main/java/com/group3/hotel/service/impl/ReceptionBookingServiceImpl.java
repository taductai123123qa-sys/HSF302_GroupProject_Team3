package com.group3.hotel.service.impl;

import com.group3.hotel.entity.BookingDetail;
import com.group3.hotel.entity.Room;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.repository.RoomRepository;
import com.group3.hotel.service.IReceptionBookingService;
import com.group3.hotel.service.RoomAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReceptionBookingServiceImpl implements IReceptionBookingService {

    private final RoomBookingRepository roomBookingRepository;
    private final RoomAllocationService roomAllocationService;
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomBooking> getReceptionBookings(BookingStatus status, String keyword, Pageable pageable) {
        return roomBookingRepository.findForReception(status, keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomBooking getBookingDetail(Long bookingId) {
        return roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt phòng với ID: " + bookingId));
    }

    @Override
    @Transactional
    public void approveBooking(Long bookingId) {
        RoomBooking booking = getBookingDetail(bookingId);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt đơn ở trạng thái PENDING.");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        roomBookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void rejectBooking(Long bookingId) {
        RoomBooking booking = getBookingDetail(bookingId);

        // Validation: Only PENDING mới có thể cancelled
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn đang ở trạng thái PENDING");
        }
        // update trạng thái là đã hủy
        booking.setBookingStatus(BookingStatus.CANCELLED);
        roomBookingRepository.save(booking);

        // Giải phóng phòng đang giữ tạm thời
        roomAllocationService.releaseRoomsForCancelledBooking(booking);
    }

    @Override
    @Transactional
    public void checkInBooking(Long bookingId, Map<Long, Long> detailRoomMap) {
        RoomBooking booking = getBookingDetail(bookingId);

        // Validate: chỉ có thể checkin với đơn hàng đã CONFIRMED
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể Check-in cho đơn đặt phòng đã được CONFIRMED.");
        }

        // Validate không tìm thấy chi tiết đơn hàng
        for (BookingDetail detail : booking.getBookingDetails()) {
            Long roomId = detailRoomMap.get(detail.getId());
            if (roomId == null) {
                throw new IllegalArgumentException("Thiếu thông tin phòng cho chi tiết đơn #" + detail.getId());
            }
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng với ID: " + roomId));

            if (room.getRoomStatus() != RoomStatus.AVAILABLE) {
                throw new IllegalStateException("Phòng " + room.getRoomNumber() + " không ở trạng thái AVAILABLE");
            }

            detail.setRoom(room);
            room.setRoomStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }

        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        roomBookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void checkOutBooking(Long bookingId) {
        RoomBooking booking = getBookingDetail(bookingId);

        // 1. Validate: chỉ có thể checkout với đơn hàng đã CHECKED_IN
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Chỉ có thể Check-out cho đơn đặt phòng đang CHECKED_IN.");
        }

        // 2. đổi trạng thái sang CHECKED_OUT
        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        roomBookingRepository.save(booking);

        // 3. đổi trạng thái phòng sang NEED_CLEANING
        for (BookingDetail detail : booking.getBookingDetails()) {
            if (detail.getRoom() != null) {
                detail.getRoom().setRoomStatus(RoomStatus.NEED_CLEANING);
                roomRepository.save(detail.getRoom());
            }
        }
    }

    @Override
    @Transactional
    public void changeRoom(Long bookingId, Long detailId, Long newRoomId, boolean keepPrice) {
        RoomBooking booking = getBookingDetail(bookingId);

        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Chỉ có thể đổi phòng cho đơn đang CHECKED_IN.");
        }

        BookingDetail targetDetail = booking.getBookingDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn phòng hợp lệ."));

        Room oldRoom = targetDetail.getRoom();
        if (oldRoom != null) {
            oldRoom.setRoomStatus(RoomStatus.NEED_CLEANING);
            roomRepository.save(oldRoom);
        }

        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng mới."));

        if (newRoom.getRoomStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Phòng mới không ở trạng thái AVAILABLE.");
        }

        newRoom.setRoomStatus(com.group3.hotel.enums.RoomStatus.OCCUPIED);
        roomRepository.save(newRoom);

        targetDetail.setRoom(newRoom);

        if (!keepPrice) {
            targetDetail.setPrice(newRoom.getRoomCategory().getPricePerNight());
            targetDetail.setRoomCategory(newRoom.getRoomCategory());

            java.math.BigDecimal newTotal = java.math.BigDecimal.ZERO;
            for (BookingDetail detail : booking.getBookingDetails()) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(),
                        booking.getCheckOutDate());
                if (nights <= 0)
                    nights = 1;
                java.math.BigDecimal detailTotal = detail.getPrice().multiply(java.math.BigDecimal.valueOf(nights));
                newTotal = newTotal.add(detailTotal);
            }
            booking.setTotalPrice(newTotal);
        } else {
            targetDetail.setRoomCategory(newRoom.getRoomCategory());
        }

        roomBookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<Room>> getAvailableRoomsByCategoryForBooking(RoomBooking booking) {
        Map<Long, List<Room>> availableRoomsMap = new HashMap<>();
        for (BookingDetail detail : booking.getBookingDetails()) {
            Long categoryId = detail.getRoomCategory().getId();
            if (!availableRoomsMap.containsKey(categoryId)) {
                availableRoomsMap.put(categoryId, roomRepository.findByRoomCategoryIdAndRoomStatus(categoryId, RoomStatus.AVAILABLE));
            }
        }
        return availableRoomsMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllAvailableRooms() {
        return roomRepository.findWithFilters(null, RoomStatus.AVAILABLE, null);
    }
}

