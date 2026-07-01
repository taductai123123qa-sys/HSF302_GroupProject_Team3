package com.group3.hotel.repository;

import com.group3.hotel.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    @Query("SELECT c.name, SUM(bd.price) " +
            "FROM BookingDetail bd " +
            "JOIN bd.roomCategory c " +
            "JOIN bd.roomBooking rb " +
            "GROUP BY c.name")
    List<Object[]> getRevenueByCategory();

    @Query("SELECT c.name, COUNT(bd.id) " +
            "FROM BookingDetail bd " +
            "JOIN bd.roomCategory c " +
            "JOIN bd.roomBooking rb " +
            "GROUP BY c.name")
    List<Object[]> getBookingCountByCategory();

    @Query("SELECT bd FROM BookingDetail bd " +
           "JOIN bd.roomBooking rb " +
           "WHERE bd.room.id = :roomId " +
           "AND rb.bookingStatus = com.group3.hotel.enums.BookingStatus.CHECKED_IN")
    List<BookingDetail> findActiveDetailByRoomId(@Param("roomId") Long roomId);
}