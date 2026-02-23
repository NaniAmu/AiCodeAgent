package com.diginest.aireceptionist.controller;

import com.diginest.aireceptionist.dto.*;
import com.diginest.aireceptionist.service.UsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @PostMapping("/start")
    public ResponseEntity<UsageResponse> startSession(@Valid @RequestBody UsageStartRequest request) {
        UsageResponse response = usageService.startSession(
                request.getHotelId(),
                request.getSessionId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/update")
    public ResponseEntity<UsageResponse> updateUsage(@Valid @RequestBody UsageUpdateRequest request) {
        UsageResponse response = usageService.updateTokenUsage(
                request.getSessionId(),
                request.getInputTokens(),
                request.getOutputTokens()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking-attempt")
    public ResponseEntity<UsageResponse> recordBookingAttempt(@RequestParam String sessionId) {
        UsageResponse response = usageService.incrementBookingAttempt(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end")
    public ResponseEntity<UsageResponse> endSession(@Valid @RequestBody UsageEndRequest request) {
        UsageResponse response = usageService.endSession(request.getSessionId());
        return ResponseEntity.ok(response);
    }
}
