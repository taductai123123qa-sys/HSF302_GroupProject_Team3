package com.group3.hotel.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class RoomSearchRequest {
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate = LocalDate.now();
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate = LocalDate.now().plusDays(1);
    
    private Integer capacity = 2;
    private Long roomCategory;
    private Double minPrice;
    private Double maxPrice;
    private Integer numberOfNights = 1;

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Long getRoomCategory() { return roomCategory; }
    public void setRoomCategory(Long roomCategory) { this.roomCategory = roomCategory; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public Integer getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(Integer numberOfNights) { this.numberOfNights = numberOfNights; }
}
