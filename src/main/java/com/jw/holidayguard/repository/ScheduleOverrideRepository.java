package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, UUID> {
    
    List<ScheduleOverride> findByScheduleIdAndVersionId(UUID scheduleId, UUID versionId);
    
    Optional<ScheduleOverride> findByScheduleIdAndVersionIdAndOverrideDate(UUID scheduleId, UUID versionId, LocalDate overrideDate);
    
    List<ScheduleOverride> findByScheduleIdAndOverrideDate(UUID scheduleId, LocalDate overrideDate);
    
    @Query("SELECT so FROM ScheduleOverride so WHERE so.scheduleId = :scheduleId AND so.versionId = :versionId AND so.overrideDate = :date AND (so.expiresAt IS NULL OR so.expiresAt >= :date)")
    Optional<ScheduleOverride> findActiveOverrideForDate(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("date") LocalDate date);
    
    @Query("SELECT so FROM ScheduleOverride so WHERE so.scheduleId = :scheduleId AND so.versionId = :versionId AND so.overrideDate BETWEEN :startDate AND :endDate AND (so.expiresAt IS NULL OR so.expiresAt >= :startDate)")
    List<ScheduleOverride> findActiveOverridesInDateRange(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT so FROM ScheduleOverride so WHERE so.scheduleId = :scheduleId AND so.versionId = :versionId AND so.overrideDate BETWEEN :startDate AND :endDate AND (so.expiresAt IS NULL OR so.expiresAt >= :startDate)")
    List<ScheduleOverride> findActiveOverridesForDateRange(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<ScheduleOverride> findByAction(ScheduleOverride.OverrideAction action);
    
    List<ScheduleOverride> findByCreatedBy(String createdBy);
    
    @Query("SELECT so FROM ScheduleOverride so WHERE so.expiresAt IS NOT NULL AND so.expiresAt < :date")
    List<ScheduleOverride> findExpiredOverrides(@Param("date") LocalDate date);
}