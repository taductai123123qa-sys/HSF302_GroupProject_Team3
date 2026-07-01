package com.group3.hotel.service;

import com.group3.hotel.entity.RoomBooking;
import com.group3.hotel.entity.RoomCategory;

public interface RoomAllocationService {
    void assignTemporaryRooms(RoomBooking booking, RoomCategory category, int roomCount);
    void releaseRoomsForCancelledBooking(RoomBooking booking);
}
