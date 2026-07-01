package com.group3.hotel.service;


import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import java.util.ArrayList;
import com.group3.hotel.repository.RoomRepository;
import com.group3.hotel.repository.BookingDetailRepository;
import com.group3.hotel.enums.RoomStatus;

@Service
public class RoomCategoryServiceImpl implements RoomCategoryService {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Override
    public List<RoomCategory> getAllCategories() {
        return roomCategoryRepository.findAll();
    }

    @Override
    public List<RoomCategory> searchCategory(RoomSearchRequest searchRequest) {

        LocalDate checkIn = (searchRequest.getCheckInDate() != null) ? searchRequest.getCheckInDate() : LocalDate.now();
        LocalDate checkout = (searchRequest.getCheckOutDate() != null) ? searchRequest.getCheckOutDate() : LocalDate.now().plusDays(1);
        Integer guests = (searchRequest.getCapacity() != null && searchRequest.getCapacity() > 0) ? searchRequest.getCapacity() : 1;

        if(!checkout.isAfter(checkIn)){
            checkout = checkIn.plusDays(1);
        }
        
        List<RoomCategory> allCategories = roomCategoryRepository.findAll();
        List<RoomCategory> available = new ArrayList<>();
        
        for (RoomCategory rc : allCategories) {
            if (rc.getCapacity() < guests) {
                continue;
            }
            
            long totalRooms = roomRepository.countByRoomCategoryIdAndRoomStatusNot(rc.getId(), RoomStatus.MAINTENANCE);
            long bookedRooms = bookingDetailRepository.countBookedRooms(rc.getId(), checkIn, checkout);
            
            int availableCount = (int) (totalRooms - bookedRooms);
            if (availableCount > 0) {
                rc.setDynamicAvailableCount(availableCount);
                available.add(rc);
            }
        }

        if (searchRequest.getRoomCategory() != null) {
            available = available.stream()
                    .filter(c -> c.getId().equals(searchRequest.getRoomCategory()))
                    .toList();
        }

        if (searchRequest.getMinPrice() != null) {
            available = available.stream()
                    .filter(c -> c.getPricePerNight().doubleValue() >= searchRequest.getMinPrice())
                    .toList();
        }

        if (searchRequest.getMaxPrice() != null) {
            available = available.stream()
                    .filter(c -> c.getPricePerNight().doubleValue() <= searchRequest.getMaxPrice())
                    .toList();
        }

        return available;
    }

    @Override
    public RoomCategory getCategoryById(Long id) {
        return roomCategoryRepository.findById(id).orElse(null);
    }

    @Override
    public int getAvailableRoomCount(Long categoryId, LocalDate checkIn, LocalDate checkOut) {
        long totalRooms = roomRepository.countByRoomCategoryIdAndRoomStatusNot(categoryId, RoomStatus.MAINTENANCE);
        long bookedRooms = bookingDetailRepository.countBookedRooms(categoryId, checkIn, checkOut);
        return (int) (totalRooms - bookedRooms);
    }
}
