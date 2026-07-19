package com.group3.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "room_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "room_size")
    private Double size;

    private String imgUrl;

    @OneToMany(mappedBy = "roomCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;

    @Transient
    private Integer dynamicAvailableCount;

    @Transient
    public int getAvailableRoomCount() {
        if (dynamicAvailableCount != null) {
            return dynamicAvailableCount;
        }
        if (rooms == null) return 0;
        return (int) rooms.stream()
                .filter(room -> room.getRoomStatus() != null && room.getRoomStatus().name().equals("AVAILABLE"))
                .count();
    }
}
