package com.diginest.aireceptionist.service;

import com.diginest.aireceptionist.dto.*;
import com.diginest.aireceptionist.entity.Hotel;
import com.diginest.aireceptionist.entity.UsageRecord;
import com.diginest.aireceptionist.exception.ResourceNotFoundException;
import com.diginest.aireceptionist.exception.UsageLimitExceededException;
import com.diginest.aireceptionist.repository.HotelRepository;
import com.diginest.aireceptionist.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageRecordRepository usageRecordRepository;
    private final HotelRepository hotelRepository;

    @Transactional
    public UsageResponse startSession(Long hotelId, String sessionId) {
        validateHotelExists(hotelId);

        if (usageRecordRepository.existsBySessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID already exists");
        }

        UsageRecord record = new UsageRecord();
        record.setHotelId(hotelId);
        record.setSessionId(sessionId);
        record.setCallStartTime(LocalDateTime.now());
        record.setStatus(UsageRecord.Status.ACTIVE);
        record.setInputTokens(0);
        record.setOutputTokens(0);
        record.setTotalTokens(0);
        record.setBookingAttempts(0);

        UsageRecord saved = usageRecordRepository.save(record);
        return mapToResponse(saved);
    }

    @Transactional
    public UsageResponse updateTokenUsage(String sessionId, Integer inputTokens, Integer outputTokens) {
        UsageRecord record = usageRecordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Usage record", "sessionId", sessionId));

        if (record.getStatus() == UsageRecord.Status.COMPLETED) {
            throw new IllegalStateException("Cannot update completed session");
        }

        int newTokens = inputTokens + outputTokens;
        int currentMonthUsage = getCurrentMonthTokenUsage(record.getHotelId());
        Hotel hotel = hotelRepository.findById(record.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", record.getHotelId()));

        if (currentMonthUsage + newTokens > hotel.getMonthlyTokenLimit()) {
            throw new UsageLimitExceededException("USAGE_LIMIT_EXCEEDED");
        }

        record.setInputTokens(record.getInputTokens() + inputTokens);
        record.setOutputTokens(record.getOutputTokens() + outputTokens);
        record.setTotalTokens(record.getTotalTokens() + newTokens);

        UsageRecord saved = usageRecordRepository.save(record);
        return mapToResponse(saved);
    }

    @Transactional
    public UsageResponse incrementBookingAttempt(String sessionId) {
        UsageRecord record = usageRecordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Usage record", "sessionId", sessionId));

        if (record.getStatus() == UsageRecord.Status.COMPLETED) {
            throw new IllegalStateException("Cannot update completed session");
        }

        record.setBookingAttempts(record.getBookingAttempts() + 1);
        UsageRecord saved = usageRecordRepository.save(record);
        return mapToResponse(saved);
    }

    @Transactional
    public UsageResponse endSession(String sessionId) {
        UsageRecord record = usageRecordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Usage record", "sessionId", sessionId));

        LocalDateTime endTime = LocalDateTime.now();
        record.setCallEndTime(endTime);
        record.setStatus(UsageRecord.Status.COMPLETED);

        if (record.getCallStartTime() != null) {
            long duration = ChronoUnit.SECONDS.between(record.getCallStartTime(), endTime);
            record.setDurationSeconds(duration);
        }

        UsageRecord saved = usageRecordRepository.save(record);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Integer getCurrentMonthTokenUsage(Long hotelId) {
        validateHotelExists(hotelId);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return usageRecordRepository.sumTotalTokensByHotelIdAndDateAfter(hotelId, startOfMonth);
    }

    private void validateHotelExists(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", "id", hotelId);
        }
    }

    private UsageResponse mapToResponse(UsageRecord record) {
        return UsageResponse.builder()
                .id(record.getId())
                .hotelId(record.getHotelId())
                .sessionId(record.getSessionId())
                .callStartTime(record.getCallStartTime())
                .callEndTime(record.getCallEndTime())
                .durationSeconds(record.getDurationSeconds())
                .inputTokens(record.getInputTokens())
                .outputTokens(record.getOutputTokens())
                .totalTokens(record.getTotalTokens())
                .bookingAttempts(record.getBookingAttempts())
                .status(record.getStatus().name())
                .build();
    }
}
