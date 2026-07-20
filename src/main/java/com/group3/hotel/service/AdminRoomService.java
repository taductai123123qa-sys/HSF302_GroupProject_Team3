package com.group3.hotel.service;

import com.group3.hotel.entity.Room;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.exception.BadRequestException;
import com.group3.hotel.exception.ResourceNotFoundException;
import com.group3.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoomService {

    private final RoomRepository roomRepository;
    private final com.group3.hotel.repository.BookingDetailRepository bookingDetailRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAllByOrderByRoomNumberAsc();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng có id = " + id));
    }

    public void saveRoom(Room room) {
        if (room.getId() == null) {
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                throw new BadRequestException("Số phòng đã tồn tại: " + room.getRoomNumber());
            }
        } else {
            if (roomRepository.existsByRoomNumberAndIdNot(room.getRoomNumber(), room.getId())) {
                throw new BadRequestException("Số phòng đã tồn tại: " + room.getRoomNumber());
            }
        }

        if (room.getFloor() == null || room.getFloor() <= 0) {
            throw new BadRequestException("Tầng phải lớn hơn 0");
        }

        if (room.getRoomStatus() == null) {
            room.setRoomStatus(RoomStatus.AVAILABLE);
        }

        roomRepository.save(room);
    }

    /**
     * Xóa phòng với các kiểm tra nghiệp vụ để tránh lỗi 500 từ ràng buộc khóa ngoại:
     * - Chặn nếu phòng đang có khách ở (OCCUPIED)
     * - Chặn nếu phòng đang nằm trong đơn đặt (kể cả PENDING/CONFIRMED) chưa kết thúc
     * - Chặn nếu phòng có lịch sử booking detail liên kết (FK constraint)
     */
    public void deleteRoom(Long id) {
        Room room = getRoomById(id);

        if (room.getRoomStatus() == RoomStatus.OCCUPIED) {
            throw new BadRequestException(
                    "Không thể xóa phòng " + room.getRoomNumber() + " vì đang có khách lưu trú.");
        }

        long activeBookings = bookingDetailRepository.findActiveDetailByRoomId(id).size();
        if (activeBookings > 0) {
            throw new BadRequestException(
                    "Không thể xóa phòng " + room.getRoomNumber() + " vì đang có đơn đặt chưa hoàn tất liên quan.");
        }

        try {
            roomRepository.delete(room);
            roomRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Không thể xóa phòng " + room.getRoomNumber()
                            + " vì đã có lịch sử đặt phòng liên quan trong hệ thống.");
        }
    }
}
