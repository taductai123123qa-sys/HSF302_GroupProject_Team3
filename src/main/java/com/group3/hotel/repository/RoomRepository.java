package com.group3.hotel.repository;

import com.group3.hotel.entity.Room;
import com.group3.hotel.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByRoomCategoryIdAndRoomStatus(Long categoryId, RoomStatus status);
}
