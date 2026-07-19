package com.group3.hotel.service.impl;

import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.entity.Payment;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.enums.PaymentMethod;
import com.group3.hotel.enums.PaymentStatus;
import com.group3.hotel.repository.PaymentRepository;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.service.IBookingService;
import com.group3.hotel.service.RoomAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.group3.hotel.repository.CustomerRepository;
import com.group3.hotel.entity.Customer;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final RoomBookingRepository roomBookingRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final RoomAllocationService roomAllocationService;

    @Override
    @Transactional
    public RoomBooking createBooking(BookingCreateRequest request, Integer depositRate, BigDecimal totalPrice, String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("User must be logged in to book a room.");
        }
        
        Customer currentCustomer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found for email: " + email));

        BigDecimal paymentAmountBd = totalPrice.multiply(BigDecimal.valueOf(depositRate)).divide(BigDecimal.valueOf(100));

        Integer finalGuestCount = (request.getGuestCount() != null && request.getGuestCount() > 0) ? request.getGuestCount() : 1;
        LocalDate finalCheckIn = request.getCheckInDate() != null ? request.getCheckInDate() : LocalDate.now();
        LocalDate finalCheckOut = request.getCheckOutDate() != null ? request.getCheckOutDate() : LocalDate.now().plusDays(1);

        RoomBooking booking = RoomBooking.builder()
                .customer(currentCustomer)
                .checkInDate(finalCheckIn)
                .checkOutDate(finalCheckOut)
                .totalPrice(totalPrice)
                .bookingStatus(BookingStatus.PENDING)
                .numberOfGuests(finalGuestCount)
                .notes(request.getNotes())
                .build();
        
        booking = roomBookingRepository.save(booking);

        RoomCategory category = roomCategoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category != null) {
            roomAllocationService.assignTemporaryRooms(booking, category, request.getRoomCount());
        }

        Payment payment = Payment.builder()
                .roomBooking(booking)
                .amount(paymentAmountBd)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentDate(LocalDateTime.now())
                .status(PaymentStatus.UNPAID)
                .txnRef(String.valueOf(booking.getId()))
                .build();
        paymentRepository.save(payment);

        return booking;
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        roomBookingRepository.save(booking);

        Payment payment = paymentRepository.findFirstByRoomBookingIdOrderByIdDesc(bookingId).orElse(null);
        if (payment != null) {
            if (payment.getAmount().compareTo(booking.getTotalPrice()) >= 0) {
                payment.setStatus(PaymentStatus.PAID);
            } else {
                payment.setStatus(PaymentStatus.PARTIAL);
            }
            paymentRepository.save(payment);
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        roomBookingRepository.save(booking);

        Payment payment = paymentRepository.findFirstByRoomBookingIdOrderByIdDesc(bookingId).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.UNPAID) {
        }
        roomAllocationService.releaseRoomsForCancelledBooking(booking);
    }

    @Override
    @Transactional
    public void cancelCustomerBooking(Long bookingId, String email) throws Exception {
        Customer customer = customerRepository.findByUserEmail(email).orElseThrow(() -> new Exception("Customer not found"));
        RoomBooking booking = roomBookingRepository.findById(bookingId).orElseThrow(() -> new Exception("Booking not found"));
        
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new Exception("You do not have permission to cancel this booking.");
        }
        
        cancelBooking(bookingId);
    }

    @Override
    @Transactional
    public void requestRoomChange(Long bookingId, String email, String reason) throws Exception {
        Customer customer = customerRepository.findByUserEmail(email).orElseThrow(() -> new Exception("Customer not found"));
        RoomBooking booking = roomBookingRepository.findById(bookingId).orElseThrow(() -> new Exception("Booking not found"));
        
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new Exception("You do not have permission to perform this action.");
        }
        
        if (booking.getBookingStatus() == BookingStatus.CHECKED_IN || booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            String prefix = booking.getBookingStatus() == BookingStatus.CHECKED_IN ? "[YÊU CẦU ĐỔI PHÒNG - " : "[YÊU CẦU NÂNG HẠNG - ";
            String dateStr = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now());
            String newNote = prefix + dateStr + "]: " + reason;
            
            if (booking.getNotes() == null || booking.getNotes().isEmpty()) {
                booking.setNotes(newNote);
            } else {
                booking.setNotes(booking.getNotes() + "\n\n" + newNote);
            }
            
            roomBookingRepository.save(booking);
        } else {
            throw new Exception("Invalid booking status for this request.");
        }
    }

    @Override
    public com.group3.hotel.dto.response.BookingHistoryDTO getCustomerBookingHistory(String email, String status, String sort) {
        Customer customer = customerRepository.findByUserEmail(email).orElse(null);
        if (customer == null) {
            return new com.group3.hotel.dto.response.BookingHistoryDTO(java.util.Collections.emptyList(), 0, 0, 0);
        }

        java.util.List<RoomBooking> allBookings = roomBookingRepository.findByCustomerOrderByCreatedAtDesc(customer);
        
        java.util.List<RoomBooking> filteredBookings = allBookings;
        if (!"ALL".equalsIgnoreCase(status)) {
            filteredBookings = allBookings.stream()
                    .filter(b -> b.getBookingStatus().name().equalsIgnoreCase(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        if ("asc".equalsIgnoreCase(sort)) {
            java.util.Collections.reverse(filteredBookings);
        }

        long countAll = allBookings.size();
        long countCheckedIn = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CHECKED_IN).count();
        long countCancelled = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED).count();

        return new com.group3.hotel.dto.response.BookingHistoryDTO(filteredBookings, countAll, countCheckedIn, countCancelled);
    }

    @Override
    public com.group3.hotel.dto.response.BookingSummaryDTO calculateBookingSummary(BookingCreateRequest request, RoomCategory category) {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) nights = 1;

        BigDecimal totalPrice = category.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.getRoomCount()));

        return new com.group3.hotel.dto.response.BookingSummaryDTO(nights, totalPrice);
    }

    @Override
    @Transactional
    public void updatePaymentGatewayTransaction(String txnRef, String transactionNo) {
        Payment payment = paymentRepository.findByTxnRef(txnRef).orElse(null);
        if (payment != null && transactionNo != null && !transactionNo.isEmpty()) {
            payment.setGatewayTransactionNo(transactionNo);
            paymentRepository.save(payment);
        }
    }
}
