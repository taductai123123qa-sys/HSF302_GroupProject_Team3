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
        
        // Validation: Only PENDING can be approved
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
}
