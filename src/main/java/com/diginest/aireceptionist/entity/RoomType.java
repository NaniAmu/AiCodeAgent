package com.diginest.aireceptionist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "room_types")
@Getter
@Setter
public class RoomType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    @Column(name = "total_rooms")
    private Integer totalRooms;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
