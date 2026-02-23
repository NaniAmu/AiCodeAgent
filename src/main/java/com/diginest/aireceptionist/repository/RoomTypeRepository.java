package com.diginest.aireceptionist.repository;

import com.diginest.aireceptionist.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findByHotelIdAndName(Long hotelId, String name);

    boolean existsByHotelIdAndNameAndIsActiveTrue(Long hotelId, String name);

    boolean existsByHotelIdAndIdAndIsActiveTrue(Long hotelId, Long id);
}
