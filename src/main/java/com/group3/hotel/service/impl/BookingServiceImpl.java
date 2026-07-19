package com.group3.hotel.service.impl;

import com.group3.hotel.dto.request.BookingCreateRequest;
import com.group3.hotel.entity.Payment;
import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.entity.User;
import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.enums.PaymentMethod;
import com.group3.hotel.enums.PaymentStatus;
import com.group3.hotel.enums.UserRole;
import com.group3.hotel.repository.PaymentRepository;
import com.group3.hotel.repository.RoomBookingRepository;
import com.group3.hotel.repository.RoomCategoryRepository;
import com.group3.hotel.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoomAllocationService roomAllocationService;

    @Override
    @Transactional
    public RoomBooking createBooking(BookingCreateRequest request, Integer depositRate, BigDecimal totalPrice, String email) {
        if (email == null) {
            email = "guest@hotel.com";
        }
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) {
            currentUser = User.builder()
                    .email(email)
                    .password("123456")
                    .role(UserRole.GUEST)
                    .build();
            currentUser = userRepository.save(currentUser);
        }

        Customer currentCustomer = customerRepository.findByUserEmail(email).orElse(null);
        if (currentCustomer == null) {
            currentCustomer = Customer.builder()
                    .user(currentUser)
                    .fullName(email)
                    .phone("0123456789")
                    .build();
            currentCustomer = customerRepository.save(currentCustomer);
        }

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
}
