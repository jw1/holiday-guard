package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    Optional<Version> findByScheduleIdAndActiveTrue(Long scheduleId);

    List<Version> findByScheduleIdOrderByCreatedAtDesc(Long scheduleId);

    List<Version> findByScheduleIdAndActiveFalseOrderByCreatedAtDesc(Long scheduleId);

    @Query("SELECT sv FROM Version sv WHERE sv.scheduleId = :scheduleId AND sv.effectiveFrom <= :asOfDate ORDER BY sv.effectiveFrom DESC")
    List<Version> findVersionsAsOf(@Param("scheduleId") Long scheduleId, @Param("asOfDate") Instant asOfDate);

    @Query("SELECT sv FROM Version sv WHERE sv.scheduleId = :scheduleId AND sv.effectiveFrom <= :asOfDate ORDER BY sv.effectiveFrom DESC LIMIT 1")
    Optional<Version> findActiveVersionAsOf(@Param("scheduleId") Long scheduleId, @Param("asOfDate") Instant asOfDate);

    boolean existsByScheduleIdAndActiveTrue(Long scheduleId);
}