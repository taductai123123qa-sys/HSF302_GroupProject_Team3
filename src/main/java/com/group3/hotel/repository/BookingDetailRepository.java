package com.group3.hotel.repository;

import com.group3.hotel.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    List<BookingDetail> findByRoomBookingId(Long bookingId);

    @Query("SELECT COUNT(bd) FROM BookingDetail bd " +
           "JOIN bd.roomBooking rb " +
           "WHERE bd.roomCategory.id = :categoryId " +
           "AND rb.bookingStatus != com.group3.hotel.enums.BookingStatus.CANCELLED " +
           "AND (:checkInDate < rb.checkOutDate AND :checkOutDate > rb.checkInDate)")
    long countBookedRooms(@Param("categoryId") Long categoryId, 
                          @Param("checkInDate") LocalDate checkInDate, 
                          @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT bd FROM BookingDetail bd " +
           "JOIN bd.roomBooking rb " +
           "WHERE bd.room.id = :roomId " +
           "AND rb.bookingStatus = com.group3.hotel.enums.BookingStatus.CHECKED_IN")
    List<BookingDetail> findActiveDetailByRoomId(@Param("roomId") Long roomId);
}
