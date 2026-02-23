package com.diginest.aireceptionist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsageEndRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
