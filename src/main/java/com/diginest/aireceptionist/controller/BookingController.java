package com.diginest.aireceptionist.controller;

import com.diginest.aireceptionist.dto.*;
import com.diginest.aireceptionist.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/check-availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @Valid @RequestBody AvailabilityCheckRequest request) {
        AvailabilityResponse response = bookingService.checkAvailability(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/modify/{id}")
    public ResponseEntity<BookingResponse> modifyBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingModifyRequest request) {
        BookingResponse response = bookingService.modifyBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByHotel(
            @PathVariable Long hotelId) {
        List<BookingResponse> bookings = bookingService.getBookingsByHotel(hotelId);
        return ResponseEntity.ok(bookings);
    }
}
