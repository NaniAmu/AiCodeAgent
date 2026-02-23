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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UsageControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    private Long hotelId;
    private String authToken;

    @BeforeEach
    void setUp() {
        usageRecordRepository.deleteAll();
        userRepository.deleteAll();
        hotelRepository.deleteAll();

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setIsActive(true);
        hotel.setMonthlyTokenLimit(1000);
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
    void startSession_Success() {
        UsageStartRequest request = new UsageStartRequest();
        request.setHotelId(hotelId);
        request.setSessionId("session-001");

        HttpEntity<UsageStartRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/start",
                HttpMethod.POST,
                entity,
                UsageResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSessionId()).isEqualTo("session-001");
        assertThat(response.getBody().getHotelId()).isEqualTo(hotelId);
        assertThat(response.getBody().getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getBody().getTotalTokens()).isEqualTo(0);
    }

    @Test
    void startSession_DuplicateSession_ReturnsBadRequest() {
        UsageStartRequest request = new UsageStartRequest();
        request.setHotelId(hotelId);
        request.setSessionId("duplicate-session");

        HttpEntity<UsageStartRequest> entity = new HttpEntity<>(request, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, entity, UsageResponse.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/start",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void startSession_InvalidHotel_ReturnsNotFound() {
        UsageStartRequest request = new UsageStartRequest();
        request.setHotelId(99999L);
        request.setSessionId("session-invalid");

        HttpEntity<UsageStartRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/start",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTokenUsage_Success() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-update");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        UsageUpdateRequest updateRequest = new UsageUpdateRequest();
        updateRequest.setSessionId("session-update");
        updateRequest.setInputTokens(100);
        updateRequest.setOutputTokens(50);

        HttpEntity<UsageUpdateRequest> entity = new HttpEntity<>(updateRequest, createAuthHeaders());

        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/update",
                HttpMethod.POST,
                entity,
                UsageResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInputTokens()).isEqualTo(100);
        assertThat(response.getBody().getOutputTokens()).isEqualTo(50);
        assertThat(response.getBody().getTotalTokens()).isEqualTo(150);
    }

    @Test
    void updateTokenUsage_MultipleUpdates_Accumulates() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-accumulate");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        for (int i = 0; i < 3; i++) {
            UsageUpdateRequest updateRequest = new UsageUpdateRequest();
            updateRequest.setSessionId("session-accumulate");
            updateRequest.setInputTokens(50);
            updateRequest.setOutputTokens(30);

            HttpEntity<UsageUpdateRequest> entity = new HttpEntity<>(updateRequest, createAuthHeaders());
            restTemplate.exchange("/api/usage/update", HttpMethod.POST, entity, UsageResponse.class);
        }

        UsageUpdateRequest finalUpdate = new UsageUpdateRequest();
        finalUpdate.setSessionId("session-accumulate");
        finalUpdate.setInputTokens(50);
        finalUpdate.setOutputTokens(30);

        HttpEntity<UsageUpdateRequest> entity = new HttpEntity<>(finalUpdate, createAuthHeaders());
        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/update", HttpMethod.POST, entity, UsageResponse.class);

        assertThat(response.getBody().getTotalTokens()).isEqualTo(240);
    }

    @Test
    void updateTokenUsage_ExceedsLimit_ReturnsPaymentRequired() {
        Hotel limitedHotel = new Hotel();
        limitedHotel.setName("Limited Hotel");
        limitedHotel.setIsActive(true);
        limitedHotel.setMonthlyTokenLimit(100);
        limitedHotel = hotelRepository.save(limitedHotel);

        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(limitedHotel.getId());
        startRequest.setSessionId("session-limited");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        UsageUpdateRequest updateRequest = new UsageUpdateRequest();
        updateRequest.setSessionId("session-limited");
        updateRequest.setInputTokens(150);
        updateRequest.setOutputTokens(50);

        HttpEntity<UsageUpdateRequest> entity = new HttpEntity<>(updateRequest, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/update",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).contains("USAGE_LIMIT_EXCEEDED");
    }

    @Test
    void updateTokenUsage_SessionNotFound_ReturnsNotFound() {
        UsageUpdateRequest updateRequest = new UsageUpdateRequest();
        updateRequest.setSessionId("nonexistent-session");
        updateRequest.setInputTokens(100);
        updateRequest.setOutputTokens(50);

        HttpEntity<UsageUpdateRequest> entity = new HttpEntity<>(updateRequest, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/update",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTokenUsage_CompletedSession_ReturnsBadRequest() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-completed");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        UsageEndRequest endRequest = new UsageEndRequest();
        endRequest.setSessionId("session-completed");
        HttpEntity<UsageEndRequest> endEntity = new HttpEntity<>(endRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/end", HttpMethod.POST, endEntity, UsageResponse.class);

        UsageUpdateRequest updateRequest = new UsageUpdateRequest();
        updateRequest.setSessionId("session-completed");
        updateRequest.setInputTokens(100);
        updateRequest.setOutputTokens(50);

        HttpEntity<UsageUpdateRequest> entity = new HttpEntity<>(updateRequest, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/update",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordBookingAttempt_Success() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-booking");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sessionId", "session-booking");

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/booking-attempt?sessionId={sessionId}",
                HttpMethod.POST,
                entity,
                UsageResponse.class,
                "session-booking"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookingAttempts()).isEqualTo(1);
    }

    @Test
    void recordBookingAttempt_MultipleAttempts_Accumulates() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-multi-booking");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int i = 0; i < 5; i++) {
            restTemplate.exchange(
                    "/api/usage/booking-attempt?sessionId={sessionId}",
                    HttpMethod.POST,
                    entity,
                    UsageResponse.class,
                    "session-multi-booking"
            );
        }

        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/booking-attempt?sessionId={sessionId}",
                HttpMethod.POST,
                entity,
                UsageResponse.class,
                "session-multi-booking"
        );

        assertThat(response.getBody().getBookingAttempts()).isEqualTo(6);
    }

    @Test
    void recordBookingAttempt_SessionNotFound_ReturnsNotFound() {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/booking-attempt?sessionId={sessionId}",
                HttpMethod.POST,
                entity,
                String.class,
                "nonexistent-session"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void endSession_Success() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-end");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        UsageEndRequest endRequest = new UsageEndRequest();
        endRequest.setSessionId("session-end");

        HttpEntity<UsageEndRequest> entity = new HttpEntity<>(endRequest, createAuthHeaders());

        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/end",
                HttpMethod.POST,
                entity,
                UsageResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getBody().getCallEndTime()).isNotNull();
        assertThat(response.getBody().getDurationSeconds()).isNotNull();
        assertThat(response.getBody().getDurationSeconds()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void endSession_WithTokenUsage_Success() {
        UsageStartRequest startRequest = new UsageStartRequest();
        startRequest.setHotelId(hotelId);
        startRequest.setSessionId("session-end-usage");
        HttpEntity<UsageStartRequest> startEntity = new HttpEntity<>(startRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/start", HttpMethod.POST, startEntity, UsageResponse.class);

        UsageUpdateRequest updateRequest = new UsageUpdateRequest();
        updateRequest.setSessionId("session-end-usage");
        updateRequest.setInputTokens(200);
        updateRequest.setOutputTokens(100);
        HttpEntity<UsageUpdateRequest> updateEntity = new HttpEntity<>(updateRequest, createAuthHeaders());
        restTemplate.exchange("/api/usage/update", HttpMethod.POST, updateEntity, UsageResponse.class);

        UsageEndRequest endRequest = new UsageEndRequest();
        endRequest.setSessionId("session-end-usage");

        HttpEntity<UsageEndRequest> entity = new HttpEntity<>(endRequest, createAuthHeaders());

        ResponseEntity<UsageResponse> response = restTemplate.exchange(
                "/api/usage/end",
                HttpMethod.POST,
                entity,
                UsageResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalTokens()).isEqualTo(300);
        assertThat(response.getBody().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void endSession_SessionNotFound_ReturnsNotFound() {
        UsageEndRequest endRequest = new UsageEndRequest();
        endRequest.setSessionId("nonexistent-session");

        HttpEntity<UsageEndRequest> entity = new HttpEntity<>(endRequest, createAuthHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/end",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unauthorizedAccess_ReturnsUnauthorized() {
        UsageStartRequest request = new UsageStartRequest();
        request.setHotelId(hotelId);
        request.setSessionId("session-unauth");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UsageStartRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usage/start",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
