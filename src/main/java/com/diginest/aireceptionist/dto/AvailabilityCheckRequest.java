package com.diginest.aireceptionist.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityCheckRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Room number is required")
    @Size(max = 10, message = "Room number must not exceed 10 characters")
    private String roomNumber;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;
}
