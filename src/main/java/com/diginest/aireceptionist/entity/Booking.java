package com.diginest.aireceptionist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CONFIRMED;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public enum Status {
        PENDING,
        CONFIRMED,
        CHECKED_IN,
        CHECKED_OUT,
        CANCELLED
    }
}
