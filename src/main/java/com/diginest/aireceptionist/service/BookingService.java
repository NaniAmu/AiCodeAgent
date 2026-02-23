package com.diginest.aireceptionist.service;

import com.diginest.aireceptionist.dto.*;
import com.diginest.aireceptionist.entity.Booking;
import com.diginest.aireceptionist.entity.Hotel;
import com.diginest.aireceptionist.exception.BookingValidationException;
import com.diginest.aireceptionist.exception.ResourceNotFoundException;
import com.diginest.aireceptionist.exception.RoomUnavailableException;
import com.diginest.aireceptionist.repository.BookingRepository;
import com.diginest.aireceptionist.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(AvailabilityCheckRequest request) {
        validateHotelExists(request.getHotelId());
        validateDateOrder(request.getCheckInDate(), request.getCheckOutDate());

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getHotelId(),
                request.getRoomNumber(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        boolean available = overlapping.isEmpty();

        return AvailabilityResponse.builder()
                .available(available)
                .roomNumber(request.getRoomNumber())
                .hotelId(request.getHotelId())
                .message(available ? "Room is available" : "Room is not available for selected dates")
                .build();
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        validateHotelExists(request.getHotelId());
        validateDateOrder(request.getCheckInDate(), request.getCheckOutDate());
        validateNotInPast(request.getCheckInDate());

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getHotelId(),
                request.getRoomNumber(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            throw new RoomUnavailableException("Room " + request.getRoomNumber() + " is already booked for the selected dates");
        }

        Booking booking = new Booking();
        booking.setHotelId(request.getHotelId());
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setGuestPhone(request.getGuestPhone());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setRoomNumber(request.getRoomNumber());
        booking.setTotalAmount(request.getTotalAmount());
        booking.setStatus(Booking.Status.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse modifyBooking(Long id, BookingModifyRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (request.getGuestName() != null) {
            booking.setGuestName(request.getGuestName());
        }
        if (request.getGuestEmail() != null) {
            booking.setGuestEmail(request.getGuestEmail());
        }
        if (request.getGuestPhone() != null) {
            booking.setGuestPhone(request.getGuestPhone());
        }
        if (request.getTotalAmount() != null) {
            booking.setTotalAmount(request.getTotalAmount());
        }

        if (request.getCheckInDate() != null || request.getCheckOutDate() != null) {
            LocalDate newCheckIn = request.getCheckInDate() != null ? request.getCheckInDate() : booking.getCheckInDate();
            LocalDate newCheckOut = request.getCheckOutDate() != null ? request.getCheckOutDate() : booking.getCheckOutDate();

            validateDateOrder(newCheckIn, newCheckOut);
            validateNotInPast(newCheckIn);

            if (!newCheckIn.equals(booking.getCheckInDate()) || !newCheckOut.equals(booking.getCheckOutDate())) {
                List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                        booking.getHotelId(),
                        booking.getRoomNumber(),
                        newCheckIn,
                        newCheckOut
                );
                overlapping.removeIf(b -> b.getId().equals(id));

                if (!overlapping.isEmpty()) {
                    throw new RoomUnavailableException("Room is already booked for the new dates");
                }

                booking.setCheckInDate(newCheckIn);
                booking.setCheckOutDate(newCheckOut);
            }
        }

        if (request.getStatus() != null) {
            try {
                booking.setStatus(Booking.Status.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BookingValidationException("Invalid status: " + request.getStatus());
            }
        }

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByHotel(Long hotelId) {
        validateHotelExists(hotelId);

        return bookingRepository.findByHotelIdOrderByCreatedAtDesc(hotelId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateHotelExists(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", "id", hotelId);
        }
    }

    private void validateDateOrder(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingValidationException("Check-out date must be after check-in date");
        }
    }

    private void validateNotInPast(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new BookingValidationException("Cannot book dates in the past");
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .hotelId(booking.getHotelId())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .roomNumber(booking.getRoomNumber())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .confirmedAt(booking.getConfirmedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
