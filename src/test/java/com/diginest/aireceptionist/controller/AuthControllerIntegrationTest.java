package com.diginest.aireceptionist.controller;

import com.diginest.aireceptionist.dto.*;
import com.diginest.aireceptionist.entity.Hotel;
import com.diginest.aireceptionist.repository.HotelRepository;
import com.diginest.aireceptionist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Long hotelId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        hotelRepository.deleteAll();

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setIsActive(true);
        hotel.setMonthlyTokenLimit(100000);
        hotel = hotelRepository.save(hotel);
        hotelId = hotel.getId();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@hotel.com");
        request.setPassword("Password123!");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setHotelId(hotelId);

        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("test@hotel.com");
        assertThat(response.getBody().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().getRole()).isEqualTo("HOTEL_ADMIN");
    }

    @Test
    void register_DuplicateEmail_ReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@hotel.com");
        request.setPassword("Password123!");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setHotelId(hotelId);

        restTemplate.postForEntity("/api/auth/register", request, UserResponse.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_InvalidEmail_ReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("Password123!");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setHotelId(hotelId);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("login@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setHotelId(hotelId);
        restTemplate.postForEntity("/api/auth/register", registerRequest, UserResponse.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@test.com");
        loginRequest.setPassword("Password123!");

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                JwtResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("login@test.com");
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("WrongPassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_WrongPassword_ReturnsUnauthorized() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("wrongpass@test.com");
        registerRequest.setPassword("CorrectPass123!");
        registerRequest.setFirstName("Wrong");
        registerRequest.setLastName("Pass");
        registerRequest.setHotelId(hotelId);
        restTemplate.postForEntity("/api/auth/register", registerRequest, UserResponse.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@test.com");
        loginRequest.setPassword("WrongPass123!");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getCurrentUser_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("me@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Me");
        registerRequest.setLastName("Test");
        registerRequest.setHotelId(hotelId);
        restTemplate.postForEntity("/api/auth/register", registerRequest, UserResponse.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("me@test.com");
        loginRequest.setPassword("Password123!");
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                JwtResponse.class
        );

        String token = loginResponse.getBody().getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                entity,
                UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("me@test.com");
        assertThat(response.getBody().getHotelId()).isEqualTo(hotelId);
    }

    @Test
    void getCurrentUser_NoToken_ReturnsUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/me",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getCurrentUser_InvalidToken_ReturnsUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
