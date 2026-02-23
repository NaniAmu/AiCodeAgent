package com.diginest.aireceptionist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsageStartRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Session ID is required")
    private String sessionId;
}
