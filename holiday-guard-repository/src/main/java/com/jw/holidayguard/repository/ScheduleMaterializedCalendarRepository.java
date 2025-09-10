package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleMaterializedCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleMaterializedCalendarRepository extends JpaRepository<ScheduleMaterializedCalendar, UUID> {
    
    Optional<ScheduleMaterializedCalendar> findByScheduleIdAndVersionIdAndOccursOn(UUID scheduleId, UUID versionId, LocalDate occursOn);
    
    List<ScheduleMaterializedCalendar> findByScheduleIdAndVersionIdAndOccursOnBetween(UUID scheduleId, UUID versionId, LocalDate startDate, LocalDate endDate);
    
    List<ScheduleMaterializedCalendar> findByScheduleIdAndVersionIdAndOccursOnAfterOrderByOccursOnAsc(UUID scheduleId, UUID versionId, LocalDate afterDate);
    
    @Query("SELECT smc FROM ScheduleMaterializedCalendar smc WHERE smc.scheduleId = :scheduleId AND smc.versionId = :versionId AND smc.occursOn >= :startDate ORDER BY smc.occursOn ASC LIMIT :limit")
    List<ScheduleMaterializedCalendar> findNextNOccurrences(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("startDate") LocalDate startDate, @Param("limit") int limit);
    
    List<ScheduleMaterializedCalendar> findByScheduleIdAndVersionIdAndStatus(UUID scheduleId, UUID versionId, ScheduleMaterializedCalendar.OccurrenceStatus status);
    
    List<ScheduleMaterializedCalendar> findByOverrideId(UUID overrideId);
    
    @Query("SELECT COUNT(smc) FROM ScheduleMaterializedCalendar smc WHERE smc.scheduleId = :scheduleId AND smc.versionId = :versionId AND smc.occursOn BETWEEN :startDate AND :endDate")
    long countOccurrencesInDateRange(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Modifying
    @Query("UPDATE ScheduleMaterializedCalendar smc SET smc.status = :status WHERE smc.scheduleId = :scheduleId AND smc.versionId = :versionId AND smc.occursOn = :date")
    int updateStatusForDate(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("date") LocalDate date, @Param("status") ScheduleMaterializedCalendar.OccurrenceStatus status);
    
    @Modifying
    @Query("DELETE FROM ScheduleMaterializedCalendar smc WHERE smc.scheduleId = :scheduleId AND smc.versionId = :versionId")
    void deleteByScheduleIdAndVersionId(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId);
    
    @Modifying
    @Query("DELETE FROM ScheduleMaterializedCalendar smc WHERE smc.scheduleId = :scheduleId AND smc.versionId = :versionId AND smc.occursOn BETWEEN :startDate AND :endDate")
    void deleteByScheduleIdAndVersionIdAndOccursOnBetween(@Param("scheduleId") UUID scheduleId, @Param("versionId") UUID versionId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}