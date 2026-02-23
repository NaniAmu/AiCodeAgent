package com.diginest.aireceptionist.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Guest name is required")
    @Size(max = 100, message = "Guest name must not exceed 100 characters")
    private String guestName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String guestEmail;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String guestPhone;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotBlank(message = "Room number is required")
    @Size(max = 10, message = "Room number must not exceed 10 characters")
    private String roomNumber;

    private BigDecimal totalAmount;
}
