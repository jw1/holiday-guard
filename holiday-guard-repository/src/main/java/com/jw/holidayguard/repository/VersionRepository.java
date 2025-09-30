package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VersionRepository extends JpaRepository<Version, UUID> {
    
    Optional<Version> findByScheduleIdAndActiveTrue(UUID scheduleId);
    
    List<Version> findByScheduleIdOrderByCreatedAtDesc(UUID scheduleId);
    
    List<Version> findByScheduleIdAndActiveFalseOrderByCreatedAtDesc(UUID scheduleId);
    
    @Query("SELECT sv FROM Version sv WHERE sv.scheduleId = :scheduleId AND sv.effectiveFrom <= :asOfDate ORDER BY sv.effectiveFrom DESC")
    List<Version> findVersionsAsOf(@Param("scheduleId") UUID scheduleId, @Param("asOfDate") Instant asOfDate);
    
    @Query("SELECT sv FROM Version sv WHERE sv.scheduleId = :scheduleId AND sv.effectiveFrom <= :asOfDate ORDER BY sv.effectiveFrom DESC LIMIT 1")
    Optional<Version> findActiveVersionAsOf(@Param("scheduleId") UUID scheduleId, @Param("asOfDate") Instant asOfDate);
    
    boolean existsByScheduleIdAndActiveTrue(UUID scheduleId);
}