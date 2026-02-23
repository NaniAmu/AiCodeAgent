package com.diginest.aireceptionist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_records")
@Getter
@Setter
public class UsageRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "call_start_time")
    private LocalDateTime callStartTime;

    @Column(name = "call_end_time")
    private LocalDateTime callEndTime;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "booking_attempts")
    private Integer bookingAttempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE,
        COMPLETED
    }
}
