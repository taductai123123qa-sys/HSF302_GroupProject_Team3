package com.group3.hotel.service;

import com.group3.hotel.entity.Room;
import java.util.List;

public interface IRoomService {
    /**
     * Lấy danh sách các phòng đang ở trạng thái AVAILABLE của một Hạng phòng
     * @param categoryId ID của hạng phòng
     * @return Danh sách Room
     */
    List<Room> getAvailableRoomsByCategory(Long categoryId);

    /**
     * Lấy danh sách phòng theo bộ lọc (Room Matrix)
     */
    List<com.group3.hotel.dto.response.RoomMatrixDTO> getRoomsWithFilters(String keyword, com.group3.hotel.enums.RoomStatus status, Long categoryId);

    /**
     * Lễ tân cập nhật trạng thái phòng (VD: NEED_CLEANING -> AVAILABLE)
     */
    void updateRoomStatus(Long roomId, com.group3.hotel.enums.RoomStatus status);
}
