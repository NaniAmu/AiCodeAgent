package com.diginest.aireceptionist.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailabilityResponse {

    private boolean available;
    private String message;
    private String roomNumber;
    private Long hotelId;
}
