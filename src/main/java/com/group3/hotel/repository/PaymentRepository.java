package com.group3.hotel.repository;

import com.group3.hotel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByRoomBookingIdOrderByIdDesc(Long roomBookingId);
}
