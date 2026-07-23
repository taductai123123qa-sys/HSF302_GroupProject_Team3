package com.group3.hotel.service.impl;

import com.group3.hotel.dto.response.RoomMatrixDTO;
import com.group3.hotel.entity.BookingDetail;
import com.group3.hotel.entity.Room;
import com.group3.hotel.enums.RoomStatus;
import com.group3.hotel.repository.BookingDetailRepository;
import com.group3.hotel.repository.RoomRepository;
import com.group3.hotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {

    private final RoomRepository roomRepository;
    private final BookingDetailRepository bookingDetailRepository;

    @Override
    public List<Room> getAvailableRoomsByCategory(Long categoryId) {
        return roomRepository.findByRoomCategoryIdAndRoomStatus(categoryId, RoomStatus.AVAILABLE);
    }

    @Override
    public List<RoomMatrixDTO> getRoomsWithFilters(String keyword, RoomStatus status, Long categoryId) {
        List<Room> rooms = roomRepository.findWithFilters(keyword, status, categoryId);
        
        return rooms.stream().map(room -> {
            RoomMatrixDTO dto = com.group3.hotel.dto.response.RoomMatrixDTO.builder()
                    .id(room.getId())
                    .roomNumber(room.getRoomNumber())
                    .roomStatus(room.getRoomStatus())
                    .roomCategoryName(room.getRoomCategory().getName())
                    .roomCategoryId(room.getRoomCategory().getId())
                    .build();
                    
            if (room.getRoomStatus() == RoomStatus.OCCUPIED) {
                List<BookingDetail> activeDetails = bookingDetailRepository.findActiveDetailByRoomId(room.getId());
                if (!activeDetails.isEmpty()) {
                    BookingDetail detail = activeDetails.get(0);
                    dto.setCurrentGuestName(detail.getRoomBooking().getCustomer().getFullName());
                    dto.setCheckOutDate(detail.getRoomBooking().getCheckOutDate());
                    dto.setNumberOfGuests(detail.getRoomBooking().getNumberOfGuests());
                }
            }
            
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng với ID: " + roomId));
                
        if (room.getRoomStatus() == RoomStatus.OCCUPIED) {
            throw new IllegalStateException("Không thể tự ý thay đổi trạng thái của phòng đang có khách (OCCUPIED).");
        }
        
        room.setRoomStatus(status);
        roomRepository.save(room);
    }
}
