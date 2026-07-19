package com.group3.hotel.entity;

import com.group3.hotel.enums.BookingStatus;
import com.group3.hotel.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @OneToMany(mappedBy = "roomBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingDetail> bookingDetails;

    @OneToMany(mappedBy = "roomBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingService> services;

    @OneToMany(mappedBy = "roomBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiredAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
            this.expiredAt = this.createdAt.plusMinutes(5);
        }
    }

    @Transient
    public PaymentStatus getPaymentStatus() {
        if (payments == null || payments.isEmpty()) {
            return PaymentStatus.UNPAID;
        }
        return payments.get(payments.size() - 1).getStatus();
    }

    @Transient
    public BigDecimal getPaidAmount() {
        if (payments == null || payments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getRemainingAmount() {
        BigDecimal paid = getPaidAmount();
        BigDecimal remaining = totalPrice.subtract(paid);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    @Transient
    public boolean hasSpecialRequest() {
        if (notes == null || notes.isEmpty()) {
            return false;
        }
        if (bookingStatus != BookingStatus.CONFIRMED && bookingStatus != BookingStatus.CHECKED_IN) {
            return false;
        }
        // Xử lý lỗi font chữ từ DB (chữ Ầ và Ạ bị biến thành ?) bằng cách nhận diện tiền tố an toàn
        return notes.contains("[YÊU C");
    }
}
