package com.group3.hotel.service;

import com.group3.hotel.dto.request.RoomSearchRequest;
import com.group3.hotel.entity.RoomCategory;

import java.util.List;

public interface RoomCategoryService {

    public List<RoomCategory> getAllCategories();
    public List<RoomCategory> searchCategory(RoomSearchRequest searchRequest);
    public RoomCategory getCategoryById(Long id);
}
