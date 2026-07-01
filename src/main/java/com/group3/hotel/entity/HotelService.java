package com.group3.hotel.entity;

import com.group3.hotel.enums.ServiceStatus;
import com.group3.hotel.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hotel_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column
    private ServiceStatus status;
}
