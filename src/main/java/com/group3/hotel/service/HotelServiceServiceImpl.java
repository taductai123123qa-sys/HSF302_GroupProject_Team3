package com.group3.hotel.service;

import com.group3.hotel.entity.HotelService;
import com.group3.hotel.repository.HotelServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HotelServiceServiceImpl implements HotelServiceService {

    @Autowired
    private HotelServiceRepository hotelServiceRepository;

    @Override
    public List<HotelService> getAllServices() {
        return hotelServiceRepository.findAll();
    }
}
