package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

    Optional<Rule> findByVersionId(Long versionId);

    Optional<Rule> findByVersionIdAndActiveTrue(Long versionId);

    Optional<Rule> findByScheduleIdAndVersionId(Long scheduleId, Long versionId);

    Optional<Rule> findByScheduleIdAndVersionIdAndActiveTrue(Long scheduleId, Long versionId);

    @Query("SELECT sr FROM Rule sr WHERE sr.versionId = :versionId AND sr.active = true AND sr.effectiveFrom <= :date ORDER BY sr.effectiveFrom DESC")
    Optional<Rule> findActiveRuleForDateAndVersion(@Param("versionId") Long versionId, @Param("date") LocalDate date);

    List<Rule> findByRuleType(Rule.RuleType ruleType);

    @Query("SELECT sr FROM Rule sr WHERE sr.scheduleId = :scheduleId AND sr.ruleType = :ruleType AND sr.active = true")
    List<Rule> findByScheduleIdAndRuleTypeAndActiveTrue(@Param("scheduleId") Long scheduleId, @Param("ruleType") Rule.RuleType ruleType);

    Optional<Rule> findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(Long scheduleId);
}