package com.group3.hotel.service.impl;

import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.service.IReceptionBookingService;
import com.group3.hotel.service.RoomAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceptionBookingServiceImpl implements IReceptionBookingService {

    private final RoomBookingRepository roomBookingRepository;
    private final RoomAllocationService roomAllocationService;
    private final com.group3.hotel.repository.RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomBooking> getReceptionBookings(BookingStatus status, String keyword, Pageable pageable) {
        return roomBookingRepository.findForReception(status, keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomBooking getBookingDetail(Long bookingId) {
        return roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
    }

    @Override
    @Transactional
    public void approveBooking(Long bookingId) {
        RoomBooking booking = getBookingDetail(bookingId);
        
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt đơn đang ở trạng thái PENDING.");
        }
        
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        roomBookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void rejectBooking(Long bookingId) {
        RoomBooking booking = getBookingDetail(bookingId);
        
        // Validation: Only PENDING or CONFIRMED can be rejected/cancelled
        if (booking.getBookingStatus() != BookingStatus.PENDING && booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể từ chối/hủy đơn đang ở trạng thái PENDING hoặc CONFIRMED.");
        }
        
        booking.setBookingStatus(BookingStatus.CANCELLED);
        roomBookingRepository.save(booking);
        
        // Giải phóng phòng đang giữ tạm thời
        roomAllocationService.releaseRoomsForCancelledBooking(booking);
    }

    @Override
    @Transactional
    public void checkInBooking(Long bookingId, java.util.Map<Long, Long> detailRoomMap) {
        RoomBooking booking = getBookingDetail(bookingId);
        
        // 1. Validate: Booking must be CONFIRMED
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể Check-in cho đơn đặt phòng đã được xác nhận (CONFIRMED).");
        }
        
        // 2. Validate details and assign rooms
        for (com.group3.hotel.entity.BookingDetail detail : booking.getBookingDetails()) {
            Long roomId = detailRoomMap.get(detail.getId());
            if (roomId == null) {
                throw new IllegalArgumentException("Thiếu thông tin phòng cho chi tiết đơn #" + detail.getId());
            }
            
            com.group3.hotel.entity.Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng với ID: " + roomId));
                
            if (room.getRoomStatus() != com.group3.hotel.enums.RoomStatus.AVAILABLE) {
                throw new IllegalStateException("Phòng " + room.getRoomNumber() + " không ở trạng thái sẵn sàng (AVAILABLE).");
            }
            
            detail.setRoom(room);
            room.setRoomStatus(com.group3.hotel.enums.RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }
        
        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        roomBookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void checkOutBooking(Long bookingId) {
        RoomBooking booking = getBookingDetail(bookingId);
        
        // 1. Validate: Booking must be CHECKED_IN
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Chỉ có thể Check-out cho đơn đặt phòng đang CHECKED_IN.");
        }
        
        // 2. Change status
        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        roomBookingRepository.save(booking);
        
        // 3. Update room status to NEED_CLEANING
        for (com.group3.hotel.entity.BookingDetail detail : booking.getBookingDetails()) {
            if (detail.getRoom() != null) {
                detail.getRoom().setRoomStatus(com.group3.hotel.enums.RoomStatus.NEED_CLEANING);
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
        
        com.group3.hotel.entity.BookingDetail targetDetail = booking.getBookingDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn phòng hợp lệ."));
                
        com.group3.hotel.entity.Room oldRoom = targetDetail.getRoom();
        if (oldRoom != null) {
            oldRoom.setRoomStatus(com.group3.hotel.enums.RoomStatus.NEED_CLEANING);
            roomRepository.save(oldRoom);
        }
        
        com.group3.hotel.entity.Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng mới."));
                
        if (newRoom.getRoomStatus() != com.group3.hotel.enums.RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Phòng mới không ở trạng thái AVAILABLE.");
        }
        
        newRoom.setRoomStatus(com.group3.hotel.enums.RoomStatus.OCCUPIED);
        roomRepository.save(newRoom);
        
        targetDetail.setRoom(newRoom);
        
        if (!keepPrice) {
            targetDetail.setPrice(newRoom.getRoomCategory().getPricePerNight());
            targetDetail.setRoomCategory(newRoom.getRoomCategory());
            
            java.math.BigDecimal newTotal = java.math.BigDecimal.ZERO;
            for (com.group3.hotel.entity.BookingDetail detail : booking.getBookingDetails()) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
                if (nights <= 0) nights = 1;
                java.math.BigDecimal detailTotal = detail.getPrice().multiply(java.math.BigDecimal.valueOf(nights));
                newTotal = newTotal.add(detailTotal);
            }
            booking.setTotalPrice(newTotal);
        } else {
            targetDetail.setRoomCategory(newRoom.getRoomCategory());
        }
        
        roomBookingRepository.save(booking);
    }
}
