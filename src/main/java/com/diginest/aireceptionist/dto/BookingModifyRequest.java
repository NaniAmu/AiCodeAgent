package com.diginest.aireceptionist.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingModifyRequest {

    @Size(max = 100, message = "Guest name must not exceed 100 characters")
    private String guestName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String guestEmail;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String guestPhone;

    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @Size(max = 10, message = "Room number must not exceed 10 characters")
    private String roomNumber;

    private BigDecimal totalAmount;

    private String status;
}
