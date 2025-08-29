package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleVersionRepository extends JpaRepository<ScheduleVersion, UUID> {
    
    Optional<ScheduleVersion> findByScheduleIdAndActiveTrue(UUID scheduleId);
    
    List<ScheduleVersion> findByScheduleIdOrderByCreatedAtDesc(UUID scheduleId);
    
    List<ScheduleVersion> findByScheduleIdAndActiveFalseOrderByCreatedAtDesc(UUID scheduleId);
    
    @Query("SELECT sv FROM ScheduleVersion sv WHERE sv.scheduleId = :scheduleId AND sv.effectiveFrom <= :asOfDate ORDER BY sv.effectiveFrom DESC")
    List<ScheduleVersion> findVersionsAsOf(@Param("scheduleId") UUID scheduleId, @Param("asOfDate") Instant asOfDate);
    
    @Query("SELECT sv FROM ScheduleVersion sv WHERE sv.scheduleId = :scheduleId AND sv.effectiveFrom <= :asOfDate ORDER BY sv.effectiveFrom DESC LIMIT 1")
    Optional<ScheduleVersion> findActiveVersionAsOf(@Param("scheduleId") UUID scheduleId, @Param("asOfDate") Instant asOfDate);
    
    boolean existsByScheduleIdAndActiveTrue(UUID scheduleId);
}