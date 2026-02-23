package com.diginest.aireceptionist.repository;

import com.diginest.aireceptionist.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId AND b.roomNumber = :roomNumber " +
           "AND b.status NOT IN ('CANCELLED') " +
           "AND (:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    List<Booking> findOverlappingBookings(@Param("hotelId") Long hotelId,
                                          @Param("roomNumber") String roomNumber,
                                          @Param("checkIn") LocalDate checkIn,
                                          @Param("checkOut") LocalDate checkOut);

    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId " +
           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "ORDER BY b.checkInDate ASC")
    List<Booking> findActiveBookingsByHotelId(@Param("hotelId") Long hotelId);

    List<Booking> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    boolean existsByHotelIdAndRoomNumberAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            Long hotelId, String roomNumber, Booking.Status status, LocalDate checkOut, LocalDate checkIn);
}
