package com.diginest.aireceptionist.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long hotelId;
    private Boolean isActive;
}
