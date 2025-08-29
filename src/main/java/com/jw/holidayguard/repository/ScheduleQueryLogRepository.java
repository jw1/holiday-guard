package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleQueryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleQueryLogRepository extends JpaRepository<ScheduleQueryLog, UUID> {
    
    List<ScheduleQueryLog> findByScheduleIdAndQueryDate(UUID scheduleId, LocalDate queryDate);
    
    List<ScheduleQueryLog> findByScheduleIdAndQueryDateBetween(UUID scheduleId, LocalDate startDate, LocalDate endDate);
    
    Page<ScheduleQueryLog> findByScheduleIdOrderByQueriedAtDesc(UUID scheduleId, Pageable pageable);
    
    List<ScheduleQueryLog> findByClientIdentifier(String clientIdentifier);
    
    List<ScheduleQueryLog> findByVersionId(UUID versionId);
    
    @Query("SELECT sql FROM ScheduleQueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.queriedAt BETWEEN :startTime AND :endTime ORDER BY sql.queriedAt DESC")
    List<ScheduleQueryLog> findByScheduleIdAndQueriedAtBetween(@Param("scheduleId") UUID scheduleId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    @Query("SELECT COUNT(sql) FROM ScheduleQueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.shouldRunResult = true AND sql.queryDate BETWEEN :startDate AND :endDate")
    long countPositiveResponsesInDateRange(@Param("scheduleId") UUID scheduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(sql) FROM ScheduleQueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.overrideApplied = true AND sql.queryDate BETWEEN :startDate AND :endDate")
    long countOverrideApplicationsInDateRange(@Param("scheduleId") UUID scheduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT sql.clientIdentifier FROM ScheduleQueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.clientIdentifier IS NOT NULL")
    List<String> findDistinctClientIdentifiersByScheduleId(@Param("scheduleId") UUID scheduleId);
    
    @Query("SELECT sql FROM ScheduleQueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.queryDate = :queryDate ORDER BY sql.queriedAt DESC LIMIT 1")
    ScheduleQueryLog findLatestQueryForScheduleAndDate(@Param("scheduleId") UUID scheduleId, @Param("queryDate") LocalDate queryDate);
}