package com.group3.hotel.dto.response;

import java.math.BigDecimal;

public class BookingSummaryDTO {
    private long nights;
    private BigDecimal totalPrice;

    public BookingSummaryDTO(long nights, BigDecimal totalPrice) {
        this.nights = nights;
        this.totalPrice = totalPrice;
    }

    public long getNights() {
        return nights;
    }

    public void setNights(long nights) {
        this.nights = nights;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
