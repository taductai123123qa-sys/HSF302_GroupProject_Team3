package com.group3.hotel.repository;

import com.group3.hotel.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory,Long> {


    @Query("SELECT rc FROM RoomCategory rc WHERE rc.capacity >= :guestCount AND " +
            "(SELECT COUNT(r) FROM Room r WHERE r.roomCategory = rc AND r.roomStatus != com.group3.hotel.enums.RoomStatus.MAINTENANCE) > " +
            "(SELECT COUNT(bd) FROM BookingDetail bd " +
            " JOIN bd.roomBooking rb " +
            " WHERE bd.roomCategory = rc " +
            " AND rb.bookingStatus != com.group3.hotel.enums.BookingStatus.CANCELLED " +
            " AND (:checkInDate < rb.checkOutDate AND :checkOutDate > rb.checkInDate))")
    List<RoomCategory> findAvailableCategories(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("guestCount") Integer guestCount);
}
