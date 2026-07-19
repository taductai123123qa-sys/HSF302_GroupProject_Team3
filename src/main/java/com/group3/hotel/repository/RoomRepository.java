package com.group3.hotel.repository;

import com.group3.hotel.entity.Room;
import com.group3.hotel.enums.RoomStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @EntityGraph(attributePaths = "roomCategory")
    List<Room> findAllByOrderByRoomNumberAsc();

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);

    long countByRoomStatus(RoomStatus roomStatus);

    List<Room> findByRoomCategoryIdAndRoomStatus(Long categoryId, RoomStatus status);
    long countByRoomCategoryIdAndRoomStatusNot(Long categoryId, RoomStatus status);
    
    java.util.Optional<Room> findByRoomNumber(String roomNumber);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR r.roomNumber LIKE %:keyword%) AND " +
           "(:status IS NULL OR r.roomStatus = :status) AND " +
           "(:categoryId IS NULL OR r.roomCategory.id = :categoryId) " +
           "ORDER BY r.roomNumber ASC")
    List<Room> findWithFilters(@org.springframework.data.repository.query.Param("keyword") String keyword,
                               @org.springframework.data.repository.query.Param("status") RoomStatus status,
                               @org.springframework.data.repository.query.Param("categoryId") Long categoryId);
}
