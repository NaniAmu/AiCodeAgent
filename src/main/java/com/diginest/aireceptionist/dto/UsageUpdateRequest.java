package com.diginest.aireceptionist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UsageUpdateRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "Input tokens is required")
    @PositiveOrZero(message = "Input tokens must be zero or positive")
    private Integer inputTokens;

    @NotNull(message = "Output tokens is required")
    @PositiveOrZero(message = "Output tokens must be zero or positive")
    private Integer outputTokens;
}
