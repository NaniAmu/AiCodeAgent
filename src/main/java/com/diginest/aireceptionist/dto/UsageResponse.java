package com.diginest.aireceptionist.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsageResponse {

    private Long id;
    private Long hotelId;
    private String sessionId;
    private LocalDateTime callStartTime;
    private LocalDateTime callEndTime;
    private Long durationSeconds;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private Integer bookingAttempts;
    private String status;
}
