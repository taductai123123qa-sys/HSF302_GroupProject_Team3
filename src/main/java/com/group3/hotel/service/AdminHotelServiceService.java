package com.group3.hotel.service;

import com.group3.hotel.entity.HotelService;
import com.group3.hotel.enums.ServiceStatus;
import com.group3.hotel.exception.BadRequestException;
import com.group3.hotel.exception.ResourceNotFoundException;
import com.group3.hotel.repository.HotelServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHotelServiceService {

    private final HotelServiceRepository hotelServiceRepository;

    public List<HotelService> getAllServices() {
        return hotelServiceRepository.findAllByOrderByNameAsc();
    }

    public HotelService getServiceById(Long id) {
        return hotelServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ có id = " + id));
    }

    public void saveService(HotelService hotelService) {
        if (hotelService.getName() == null || hotelService.getName().trim().isEmpty()) {
            throw new BadRequestException("Tên dịch vụ không được để trống");
        }
        if (hotelService.getPrice() == null || hotelService.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá dịch vụ không hợp lệ");
        }
        if (hotelService.getStatus() == null) {
            hotelService.setStatus(ServiceStatus.ACTIVE);
        }
        hotelServiceRepository.save(hotelService);
    }

    public void deleteService(Long id) {
        HotelService hotelService = getServiceById(id);
        hotelServiceRepository.delete(hotelService);
    }
}
