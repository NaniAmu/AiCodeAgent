package com.diginest.aireceptionist.config;

import com.diginest.aireceptionist.entity.*;
import com.diginest.aireceptionist.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding for development environment...");

        if (hotelRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        // 1. Create Hotel
        Hotel hotel = new Hotel();
        hotel.setName("Grand Plaza Hotel");
        hotel.setAddress("123 Main Street, New York, NY 10001");
        hotel.setPhone("+1-555-0100");
        hotel.setEmail("info@grandplaza.com");
        hotel.setIsActive(true);
        hotel.setMonthlyTokenLimit(100000);
        hotel = hotelRepository.save(hotel);
        log.info("Created hotel: {}", hotel.getName());

        // 2. Create Super Admin (no hotel)
        User superAdmin = new User();
        superAdmin.setEmail("superadmin@diginest.com");
        superAdmin.setPassword(passwordEncoder.encode("SuperAdmin123!"));
        superAdmin.setFirstName("System");
        superAdmin.setLastName("Administrator");
        superAdmin.setRole(User.Role.SUPER_ADMIN);
        superAdmin.setIsActive(true);
        superAdmin.setHotelId(1L); // System hotel reference
        userRepository.save(superAdmin);
        log.info("Created super admin: {}", superAdmin.getEmail());

        // 3. Create Hotel Admin
        User hotelAdmin = new User();
        hotelAdmin.setEmail("admin@grandplaza.com");
        hotelAdmin.setPassword(passwordEncoder.encode("HotelAdmin123!"));
        hotelAdmin.setFirstName("John");
        hotelAdmin.setLastName("Manager");
        hotelAdmin.setRole(User.Role.HOTEL_ADMIN);
        hotelAdmin.setIsActive(true);
        hotelAdmin.setHotelId(hotel.getId());
        userRepository.save(hotelAdmin);
        log.info("Created hotel admin: {}", hotelAdmin.getEmail());

        // 4. Create Room Types
        RoomType standard = createRoomType(hotel.getId(), "Standard", "Comfortable room with city view", new BigDecimal("150.00"), 2, 50);
        RoomType deluxe = createRoomType(hotel.getId(), "Deluxe", "Spacious room with king bed and premium amenities", new BigDecimal("250.00"), 3, 30);
        RoomType suite = createRoomType(hotel.getId(), "Suite", "Luxury suite with separate living area", new BigDecimal("450.00"), 4, 10);
        log.info("Created {} room types", 3);

        // 5. Create Bookings
        LocalDate today = LocalDate.now();

        Booking booking1 = createBooking(hotel.getId(), "Alice Johnson", "alice@email.com", "+1-555-0201",
                today.plusDays(1), today.plusDays(3), "101", new BigDecimal("300.00"));

        Booking booking2 = createBooking(hotel.getId(), "Bob Smith", "bob@email.com", "+1-555-0202",
                today.plusDays(5), today.plusDays(7), "205", new BigDecimal("500.00"));

        Booking booking3 = createBooking(hotel.getId(), "Carol White", "carol@email.com", "+1-555-0203",
                today.plusDays(10), today.plusDays(14), "301", new BigDecimal("1800.00"));

        log.info("Created {} bookings", 3);
        log.info("Data seeding completed successfully!");
    }

    private RoomType createRoomType(Long hotelId, String name, String description, BigDecimal price, int occupancy, int totalRooms) {
        RoomType roomType = new RoomType();
        roomType.setHotelId(hotelId);
        roomType.setName(name);
        roomType.setDescription(description);
        roomType.setBasePrice(price);
        roomType.setMaxOccupancy(occupancy);
        roomType.setTotalRooms(totalRooms);
        roomType.setIsActive(true);
        return roomTypeRepository.save(roomType);
    }

    private Booking createBooking(Long hotelId, String guestName, String email, String phone,
                                  LocalDate checkIn, LocalDate checkOut, String roomNumber, BigDecimal amount) {
        Booking booking = new Booking();
        booking.setHotelId(hotelId);
        booking.setGuestName(guestName);
        booking.setGuestEmail(email);
        booking.setGuestPhone(phone);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setRoomNumber(roomNumber);
        booking.setTotalAmount(amount);
        booking.setStatus(Booking.Status.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }
}
