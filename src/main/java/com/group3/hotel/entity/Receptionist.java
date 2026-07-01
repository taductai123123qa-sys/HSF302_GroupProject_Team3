package com.group3.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receptionists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receptionist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    private String avatarUrl;

    private String shiftType;
}
