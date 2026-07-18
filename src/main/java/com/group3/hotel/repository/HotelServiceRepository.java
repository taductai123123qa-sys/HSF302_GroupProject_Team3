package com.group3.hotel.repository;

import com.group3.hotel.entity.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelServiceRepository extends JpaRepository<HotelService, Long> {
    List<HotelService> findAllByOrderByNameAsc();
}
