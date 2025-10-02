package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.QueryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {

    List<QueryLog> findByScheduleIdAndQueryDate(Long scheduleId, LocalDate queryDate);

    List<QueryLog> findByScheduleIdAndQueryDateBetween(Long scheduleId, LocalDate startDate, LocalDate endDate);

    Page<QueryLog> findByScheduleIdOrderByQueriedAtDesc(Long scheduleId, Pageable pageable);

    List<QueryLog> findByClientIdentifier(String clientIdentifier);

    List<QueryLog> findByVersionId(Long versionId);

    @Query("SELECT sql FROM QueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.queriedAt BETWEEN :startTime AND :endTime ORDER BY sql.queriedAt DESC")
    List<QueryLog> findByScheduleIdAndQueriedAtBetween(@Param("scheduleId") Long scheduleId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    @Query("SELECT COUNT(sql) FROM QueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.shouldRunResult = true AND sql.queryDate BETWEEN :startDate AND :endDate")
    long countPositiveResponsesInDateRange(@Param("scheduleId") Long scheduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(sql) FROM QueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.deviationApplied = true AND sql.queryDate BETWEEN :startDate AND :endDate")
    long countOverrideApplicationsInDateRange(@Param("scheduleId") Long scheduleId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT sql.clientIdentifier FROM QueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.clientIdentifier IS NOT NULL")
    List<String> findDistinctClientIdentifiersByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("SELECT sql FROM QueryLog sql WHERE sql.scheduleId = :scheduleId AND sql.queryDate = :queryDate ORDER BY sql.queriedAt DESC LIMIT 1")
    QueryLog findLatestQueryForScheduleAndDate(@Param("scheduleId") Long scheduleId, @Param("queryDate") LocalDate queryDate);
}