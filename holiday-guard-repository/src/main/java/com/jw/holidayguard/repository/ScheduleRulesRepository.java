package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRulesRepository extends JpaRepository<ScheduleRules, UUID> {
    
    List<ScheduleRules> findByVersionIdAndActiveTrue(UUID versionId);
    
    List<ScheduleRules> findByScheduleIdAndVersionId(UUID scheduleId, UUID versionId);
    
    List<ScheduleRules> findByScheduleIdAndVersionIdAndActiveTrue(UUID scheduleId, UUID versionId);
    
    @Query("SELECT sr FROM ScheduleRules sr WHERE sr.versionId = :versionId AND sr.active = true AND sr.effectiveFrom <= :date ORDER BY sr.effectiveFrom DESC")
    List<ScheduleRules> findActiveRulesForDateAndVersion(@Param("versionId") UUID versionId, @Param("date") LocalDate date);
    
    List<ScheduleRules> findByRuleType(ScheduleRules.RuleType ruleType);
    
    @Query("SELECT sr FROM ScheduleRules sr WHERE sr.scheduleId = :scheduleId AND sr.ruleType = :ruleType AND sr.active = true")
    List<ScheduleRules> findByScheduleIdAndRuleTypeAndActiveTrue(@Param("scheduleId") UUID scheduleId, @Param("ruleType") ScheduleRules.RuleType ruleType);

    Optional<ScheduleRules> findFirstByScheduleIdOrderByCreatedAtDesc(UUID scheduleId);
}