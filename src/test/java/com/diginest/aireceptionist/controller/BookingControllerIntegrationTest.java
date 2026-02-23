package com.diginest.aireceptionist.controller;

import com.diginest.aireceptionist.dto.*;
import com.diginest.aireceptionist.entity.*;
import com.diginest.aireceptionist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    private Long hotelId;
    private String authToken;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        hotelRepository.deleteAll();

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setIsActive(true);
        hotel.setMonthlyTokenLimit(100000);
        hotel = hotelRepository.save(hotel);
        hotelId = hotel.getId();

        authToken = getAuthToken();
    }

    private String getAuthToken() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("admin@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Admin");
        registerRequest.setLastName("Test");
        registerRequest.setHotelId(hotelId);
        restTemplate.postForEntity("/api/auth/register", registerRequest, UserResponse.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("Password123!");
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, JwtResponse.class);
        return response.getBody().getToken();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return headers;
    }

    @Test
    void checkAvailability_RoomAvailable_ReturnsTrue() {
        AvailabilityCheckRequest request = new AvailabilityCheckRequest();
        request.setHotelId(hotelId);
        request.setRoomNumber("101");
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        HttpEntity<AvailabilityCheckRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<AvailabilityResponse> response = restTemplate.exchange(
                "/api/bookings/check-availability",
                HttpMethod.POST,
                entity,
                AvailabilityResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAvailable()).isTrue();
    }

    @Test
    void checkAvailability_RoomNotAvailable_ReturnsFalse() {
        BookingCreateRequest booking = new BookingCreateRequest();
        booking.setHotelId(hotelId);
        booking.setGuestName("Test Guest");
        booking.setGuestEmail("test@test.com");
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setRoomNumber("102");
        booking.setTotalAmount(new BigDecimal("300.00"));

        HttpEntity<BookingCreateRequest> bookingEntity = new HttpEntity<>(booking, createAuthHeaders());
        restTemplate.exchange("/api/bookings/create", HttpMethod.POST, bookingEntity, BookingResponse.class);

        AvailabilityCheckRequest request = new AvailabilityCheckRequest();
        request.setHotelId(hotelId);
        request.setRoomNumber("102");
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        HttpEntity<AvailabilityCheckRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<AvailabilityResponse> response = restTemplate.exchange(
                "/api/bookings/check-availability",
                HttpMethod.POST,
                entity,
                AvailabilityResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAvailable()).isFalse();
    }

    @Test
    void createBooking_Success() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setHotelId(hotelId);
        request.setGuestName("John Doe");
        request.setGuestEmail("john@email.com");
        request.setGuestPhone("+1-555-0100");
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(5));
        request.setRoomNumber("201");
        request.setTotalAmount(new BigDecimal("500.00"));

        HttpEntity<BookingCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<BookingResponse> response = restTemplate.exchange(
                "/api/bookings/create",
                HttpMethod.POST,
                entity,
                BookingResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getGuestName()).isEqualTo("John Doe");
        assertThat(response.getBody().getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getBody().getHotelId()).isEqualTo(hotelId);
    }

    @Test
    void createBooking_InvalidDates_ReturnsBadRequest() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setHotelId(hotelId);
        request.setGuestName("John Doe");
        request.setGuestEmail("john@email.com");
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(1));
        request.setRoomNumber("202");
        request.setTotalAmount(new BigDecimal("500.00"));

        HttpEntity<BookingCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/create",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBooking_PastDates_ReturnsBadRequest() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setHotelId(hotelId);
        request.setGuestName("John Doe");
        request.setGuestEmail("john@email.com");
        request.setCheckInDate(LocalDate.now().minusDays(5));
        request.setCheckOutDate(LocalDate.now().minusDays(1));
        request.setRoomNumber("203");
        request.setTotalAmount(new BigDecimal("500.00"));

        HttpEntity<BookingCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/create",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBooking_Overlapping_ReturnsConflict() {
        BookingCreateRequest request1 = new BookingCreateRequest();
        request1.setHotelId(hotelId);
        request1.setGuestName("Guest One");
        request1.setGuestEmail("guest1@email.com");
        request1.setCheckInDate(LocalDate.now().plusDays(1));
        request1.setCheckOutDate(LocalDate.now().plusDays(5));
        request1.setRoomNumber("301");
        request1.setTotalAmount(new BigDecimal("400.00"));

        HttpEntity<BookingCreateRequest> entity1 = new HttpEntity<>(request1, createAuthHeaders());
        restTemplate.exchange("/api/bookings/create", HttpMethod.POST, entity1, BookingResponse.class);

        BookingCreateRequest request2 = new BookingCreateRequest();
        request2.setHotelId(hotelId);
        request2.setGuestName("Guest Two");
        request2.setGuestEmail("guest2@email.com");
        request2.setCheckInDate(LocalDate.now().plusDays(2));
        request2.setCheckOutDate(LocalDate.now().plusDays(4));
        request2.setRoomNumber("301");
        request2.setTotalAmount(new BigDecimal("400.00"));

        HttpEntity<BookingCreateRequest> entity2 = new HttpEntity<>(request2, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/create",
                HttpMethod.POST,
                entity2,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void modifyBooking_Success() {
        BookingCreateRequest createRequest = new BookingCreateRequest();
        createRequest.setHotelId(hotelId);
        createRequest.setGuestName("Original Name");
        createRequest.setGuestEmail("original@email.com");
        createRequest.setCheckInDate(LocalDate.now().plusDays(1));
        createRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        createRequest.setRoomNumber("401");
        createRequest.setTotalAmount(new BigDecimal("300.00"));

        HttpEntity<BookingCreateRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        ResponseEntity<BookingResponse> createResponse = restTemplate.exchange(
                "/api/bookings/create", HttpMethod.POST, createEntity, BookingResponse.class);

        Long bookingId = createResponse.getBody().getId();

        BookingModifyRequest modifyRequest = new BookingModifyRequest();
        modifyRequest.setGuestName("Updated Name");
        modifyRequest.setGuestEmail("updated@email.com");

        HttpEntity<BookingModifyRequest> modifyEntity = new HttpEntity<>(modifyRequest, createAuthHeaders());

        ResponseEntity<BookingResponse> response = restTemplate.exchange(
                "/api/bookings/modify/" + bookingId,
                HttpMethod.PUT,
                modifyEntity,
                BookingResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getGuestName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getGuestEmail()).isEqualTo("updated@email.com");
    }

    @Test
    void modifyBooking_NotFound_ReturnsNotFound() {
        BookingModifyRequest modifyRequest = new BookingModifyRequest();
        modifyRequest.setGuestName("Updated Name");

        HttpEntity<BookingModifyRequest> modifyEntity = new HttpEntity<>(modifyRequest, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/modify/99999",
                HttpMethod.PUT,
                modifyEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void cancelBooking_Success() {
        BookingCreateRequest createRequest = new BookingCreateRequest();
        createRequest.setHotelId(hotelId);
        createRequest.setGuestName("Cancel Me");
        createRequest.setGuestEmail("cancel@email.com");
        createRequest.setCheckInDate(LocalDate.now().plusDays(1));
        createRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        createRequest.setRoomNumber("501");
        createRequest.setTotalAmount(new BigDecimal("200.00"));

        HttpEntity<BookingCreateRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        ResponseEntity<BookingResponse> createResponse = restTemplate.exchange(
                "/api/bookings/create", HttpMethod.POST, createEntity, BookingResponse.class);

        Long bookingId = createResponse.getBody().getId();

        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/bookings/cancel/" + bookingId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void cancelBooking_NotFound_ReturnsNotFound() {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/cancel/99999",
                HttpMethod.DELETE,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBookingsByHotel_Success() {
        BookingCreateRequest request1 = new BookingCreateRequest();
        request1.setHotelId(hotelId);
        request1.setGuestName("Guest A");
        request1.setGuestEmail("guesta@email.com");
        request1.setCheckInDate(LocalDate.now().plusDays(1));
        request1.setCheckOutDate(LocalDate.now().plusDays(3));
        request1.setRoomNumber("601");
        request1.setTotalAmount(new BigDecimal("250.00"));

        HttpEntity<BookingCreateRequest> entity1 = new HttpEntity<>(request1, createAuthHeaders());
        restTemplate.exchange("/api/bookings/create", HttpMethod.POST, entity1, BookingResponse.class);

        BookingCreateRequest request2 = new BookingCreateRequest();
        request2.setHotelId(hotelId);
        request2.setGuestName("Guest B");
        request2.setGuestEmail("guestb@email.com");
        request2.setCheckInDate(LocalDate.now().plusDays(5));
        request2.setCheckOutDate(LocalDate.now().plusDays(7));
        request2.setRoomNumber("602");
        request2.setTotalAmount(new BigDecimal("350.00"));

        HttpEntity<BookingCreateRequest> entity2 = new HttpEntity<>(request2, createAuthHeaders());
        restTemplate.exchange("/api/bookings/create", HttpMethod.POST, entity2, BookingResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

        ResponseEntity<BookingResponse[]> response = restTemplate.exchange(
                "/api/bookings/hotel/" + hotelId,
                HttpMethod.GET,
                entity,
                BookingResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getBookingsByHotel_NotFound_ReturnsNotFound() {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/hotel/99999",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unauthorizedAccess_ReturnsUnauthorized() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setHotelId(hotelId);
        request.setGuestName("Test");
        request.setGuestEmail("test@email.com");
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        request.setRoomNumber("701");
        request.setTotalAmount(new BigDecimal("100.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingCreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/create",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
