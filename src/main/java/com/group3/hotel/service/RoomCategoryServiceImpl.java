package com.group3.hotel.service;


import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomCategoryServiceImpl implements RoomCategoryService {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;

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
        
        List<RoomCategory> available = roomCategoryRepository.findAvailableCategories(checkIn, checkout, guests);

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
}
