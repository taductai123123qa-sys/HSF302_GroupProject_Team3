package com.group3.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;

    private String phone;

    private String avatarUrl;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(columnDefinition = "int default 0")
    private Integer loyaltyPoints;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomBooking> bookings;
}
