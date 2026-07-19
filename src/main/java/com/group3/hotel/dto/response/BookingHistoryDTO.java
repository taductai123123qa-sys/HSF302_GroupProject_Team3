package com.group3.hotel.dto.response;

import com.group3.hotel.entity.RoomBooking;
import java.util.List;

public class BookingHistoryDTO {
    private List<RoomBooking> bookings;
    private long countAll;
    private long countCheckedIn;
    private long countCancelled;

    public BookingHistoryDTO(List<RoomBooking> bookings, long countAll, long countCheckedIn, long countCancelled) {
        this.bookings = bookings;
        this.countAll = countAll;
        this.countCheckedIn = countCheckedIn;
        this.countCancelled = countCancelled;
    }

    public List<RoomBooking> getBookings() {
        return bookings;
    }

    public void setBookings(List<RoomBooking> bookings) {
        this.bookings = bookings;
    }

    public long getCountAll() {
        return countAll;
    }

    public void setCountAll(long countAll) {
        this.countAll = countAll;
    }

    public long getCountCheckedIn() {
        return countCheckedIn;
    }

    public void setCountCheckedIn(long countCheckedIn) {
        this.countCheckedIn = countCheckedIn;
    }

    public long getCountCancelled() {
        return countCancelled;
    }

    public void setCountCancelled(long countCancelled) {
        this.countCancelled = countCancelled;
    }
}
