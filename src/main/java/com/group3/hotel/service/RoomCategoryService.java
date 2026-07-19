package com.group3.hotel.service;

import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.entity.RoomCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomCategoryService {

    public List<RoomCategory> getAllCategories();
    public Page<RoomCategory> searchCategory(RoomSearchRequest searchRequest, Pageable pageable);
    public List<RoomCategory> searchCategory(RoomSearchRequest searchRequest); // Keep this for existing usage if any
    public RoomCategory getCategoryById(Long id);
    public int getAvailableRoomCount(Long categoryId, java.time.LocalDate checkIn, java.time.LocalDate checkOut);
}
