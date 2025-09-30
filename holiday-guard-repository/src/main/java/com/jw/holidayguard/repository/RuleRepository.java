package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleRepository extends JpaRepository<Rule, UUID> {

    Optional<Rule> findByVersionId(UUID versionId);

    Optional<Rule> findByVersionIdAndActiveTrue(UUID versionId);

    Optional<Rule> findByScheduleIdAndVersionId(UUID scheduleId, UUID versionId);

    Optional<Rule> findByScheduleIdAndVersionIdAndActiveTrue(UUID scheduleId, UUID versionId);

    @Query("SELECT sr FROM Rule sr WHERE sr.versionId = :versionId AND sr.active = true AND sr.effectiveFrom <= :date ORDER BY sr.effectiveFrom DESC")
    Optional<Rule> findActiveRuleForDateAndVersion(@Param("versionId") UUID versionId, @Param("date") LocalDate date);

    List<Rule> findByRuleType(Rule.RuleType ruleType);

    @Query("SELECT sr FROM Rule sr WHERE sr.scheduleId = :scheduleId AND sr.ruleType = :ruleType AND sr.active = true")
    List<Rule> findByScheduleIdAndRuleTypeAndActiveTrue(@Param("scheduleId") UUID scheduleId, @Param("ruleType") Rule.RuleType ruleType);

    Optional<Rule> findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(UUID scheduleId);
}