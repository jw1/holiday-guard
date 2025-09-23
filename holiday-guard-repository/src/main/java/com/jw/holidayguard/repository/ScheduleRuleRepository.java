package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRuleRepository extends JpaRepository<ScheduleRule, UUID> {

    Optional<ScheduleRule> findByVersionId(UUID versionId);

    Optional<ScheduleRule> findByVersionIdAndActiveTrue(UUID versionId);

    Optional<ScheduleRule> findByScheduleIdAndVersionId(UUID scheduleId, UUID versionId);

    Optional<ScheduleRule> findByScheduleIdAndVersionIdAndActiveTrue(UUID scheduleId, UUID versionId);

    @Query("SELECT sr FROM ScheduleRule sr WHERE sr.versionId = :versionId AND sr.active = true AND sr.effectiveFrom <= :date ORDER BY sr.effectiveFrom DESC")
    Optional<ScheduleRule> findActiveRuleForDateAndVersion(@Param("versionId") UUID versionId, @Param("date") LocalDate date);

    List<ScheduleRule> findByRuleType(ScheduleRule.RuleType ruleType);

    @Query("SELECT sr FROM ScheduleRule sr WHERE sr.scheduleId = :scheduleId AND sr.ruleType = :ruleType AND sr.active = true")
    List<ScheduleRule> findByScheduleIdAndRuleTypeAndActiveTrue(@Param("scheduleId") UUID scheduleId, @Param("ruleType") ScheduleRule.RuleType ruleType);

    Optional<ScheduleRule> findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(UUID scheduleId);
}