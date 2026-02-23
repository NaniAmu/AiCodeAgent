package com.diginest.aireceptionist.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long hotelId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String roomNumber;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
