package com.group3.hotel.repository;

import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
    List<RoomBooking> findByBookingStatusAndExpiredAtBefore(BookingStatus status, LocalDateTime now);
    List<RoomBooking> findByCustomerOrderByCreatedAtDesc(com.group3.hotel.entity.Customer customer);

    @Query("SELECT rb FROM RoomBooking rb WHERE " +
           "(:status IS NULL OR rb.bookingStatus = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "CAST(rb.id AS string) LIKE %:keyword% OR " +
           "LOWER(rb.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "rb.customer.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<RoomBooking> findForReception(@Param("status") BookingStatus status, @Param("keyword") String keyword, Pageable pageable);
}
