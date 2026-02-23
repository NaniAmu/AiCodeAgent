package com.diginest.aireceptionist.repository;

import com.diginest.aireceptionist.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    Optional<UsageRecord> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);

    @Query("SELECT COALESCE(SUM(u.totalTokens), 0) FROM UsageRecord u " +
           "WHERE u.hotelId = :hotelId " +
           "AND u.callStartTime >= :startOfMonth")
    Integer sumTotalTokensByHotelIdAndDateAfter(@Param("hotelId") Long hotelId,
                                                @Param("startOfMonth") LocalDateTime startOfMonth);
}
