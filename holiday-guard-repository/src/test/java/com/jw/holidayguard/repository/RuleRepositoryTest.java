package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Version;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jw.holidayguard.domain.Rule.RuleType.ALL_DAYS;
import static com.jw.holidayguard.domain.Rule.RuleType.WEEKDAYS_ONLY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RuleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Test
    void findByVersionIdAndActiveTrue() {
        // given - a schedule with a version and an active rule
        Schedule schedule = createSchedule("Test Schedule", "Test Description");
        Version version = createVersion(schedule.getId());
        Rule activeRule = createRule(schedule.getId(), version.getId(), WEEKDAYS_ONLY, true);

        // and - another schedule with a version and an active rule
        Schedule otherSchedule = createSchedule("Other Schedule", "Other Description");
        Version otherVersion = createVersion(otherSchedule.getId());
        createRule(otherSchedule.getId(), otherVersion.getId(), WEEKDAYS_ONLY, true);

        // when - the active rule is requested by version id
        Optional<Rule> foundRule = ruleRepository.findByVersionIdAndActiveTrue(version.getId());

        // then - the correct rule is found
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getId()).isEqualTo(activeRule.getId());
        assertThat(foundRule.get().isActive()).isTrue();
    }

    @Test
    void findByVersionId() {
        // given - a schedule with a version and a rule
        Schedule schedule = createSchedule("Test Schedule", "Test Description");
        Version version = createVersion(schedule.getId());
        Rule rule = createRule(schedule.getId(), version.getId(), WEEKDAYS_ONLY, true);

        // when - the rule is requested by version id
        Optional<Rule> foundRule = ruleRepository.findByVersionId(version.getId());

        // then - the rule is found
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getId()).isEqualTo(rule.getId());
    }

    @Test
    void findByScheduleIdAndVersionId() {
        // given - a schedule with a version and a rule
        Schedule schedule = createSchedule("Test Schedule", "Test Description");
        Version version = createVersion(schedule.getId());
        Rule rule = createRule(schedule.getId(), version.getId(), WEEKDAYS_ONLY, true);

        // and - another schedule with a version and a rule
        Schedule otherSchedule = createSchedule("Other Schedule", "Other Description");
        Version otherVersion = createVersion(otherSchedule.getId());
        createRule(otherSchedule.getId(), otherVersion.getId(), ALL_DAYS, true);

        // when - the rule is requested by schedule id and version id
        Optional<Rule> foundRule = ruleRepository.findByScheduleIdAndVersionId(schedule.getId(), version.getId());

        // then - the correct rule is found
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getId()).isEqualTo(rule.getId());
    }

    @Test
    void findByScheduleIdAndVersionIdAndActiveTrue() {
        // given - a schedule with a version and an active rule
        Schedule schedule = createSchedule("Test Schedule", "Test Description");
        Version version = createVersion(schedule.getId());
        Rule activeRule = createRule(schedule.getId(), version.getId(), WEEKDAYS_ONLY, true);

        // and - another schedule with a version and an inactive rule
        Schedule otherSchedule = createSchedule("Other Schedule", "Other Description");
        Version otherVersion = createVersion(otherSchedule.getId());
        createRule(otherSchedule.getId(), otherVersion.getId(), ALL_DAYS, false);

        // when - the active rule is requested by schedule id and version id
        Optional<Rule> foundRule = ruleRepository.findByScheduleIdAndVersionIdAndActiveTrue(schedule.getId(), version.getId());

        // then - the correct active rule is found
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getId()).isEqualTo(activeRule.getId());
        assertThat(foundRule.get().isActive()).isTrue();
    }

    @Test
    void findActiveRuleForDateAndVersion() {
        // given - a schedule with a version and an active rule with an effective from date
        Schedule schedule = createSchedule("Test Schedule", "Test Description");
        Version version = createVersion(schedule.getId());
        LocalDate from = LocalDate.now().minusDays(1);
        Rule rule = createRule(schedule.getId(), version.getId(), WEEKDAYS_ONLY, true, from);

        // when - the active rule is requested for a date after the effective from date
        Optional<Rule> foundRule = ruleRepository.findActiveRuleForDateAndVersion(version.getId(), LocalDate.now());

        // then - the rule is found
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getId()).isEqualTo(rule.getId());

        // when - the active rule is requested for a date before the effective from date
        Optional<Rule> notFoundRule = ruleRepository.findActiveRuleForDateAndVersion(version.getId(), from.minusDays(1));

        // then - the rule is not found
        assertThat(notFoundRule).isNotPresent();
    }

    @Test
    void findByRuleType() {
        // given - two schedules with different rule types
        Schedule schedule1 = createSchedule("Schedule 1", "");
        Version version1 = createVersion(schedule1.getId());
        createRule(schedule1.getId(), version1.getId(), WEEKDAYS_ONLY, true);

        Schedule schedule2 = createSchedule("Schedule 2", "");
        Version version2 = createVersion(schedule2.getId());
        createRule(schedule2.getId(), version2.getId(), ALL_DAYS, true);

        // when - rules are requested by rule type
        List<Rule> weekdaysRules = ruleRepository.findByRuleType(WEEKDAYS_ONLY);
        List<Rule> allDaysRules = ruleRepository.findByRuleType(ALL_DAYS);

        // then - the correct number of rules is returned for each type
        assertThat(weekdaysRules).hasSize(1);
        assertThat(allDaysRules).hasSize(1);
    }

    @Test
    void findByScheduleIdAndRuleTypeAndActiveTrue() {
        // given - a schedule with a specific rule type
        Schedule schedule = createSchedule("Test Schedule", "");
        Version version = createVersion(schedule.getId());
        createRule(schedule.getId(), version.getId(), WEEKDAYS_ONLY, true);

        // and - another schedule with a different rule type
        Schedule otherSchedule = createSchedule("Other Schedule", "");
        Version otherVersion = createVersion(otherSchedule.getId());
        createRule(otherSchedule.getId(), otherVersion.getId(), ALL_DAYS, true);

        // when - rules are requested by schedule id and rule type
        List<Rule> foundRules = ruleRepository.findByScheduleIdAndRuleTypeAndActiveTrue(schedule.getId(), WEEKDAYS_ONLY);

        // then - the correct rule is found
        assertThat(foundRules).hasSize(1);
        assertThat(foundRules.getFirst().getRuleType()).isEqualTo(WEEKDAYS_ONLY);
    }

    @Test
    void findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc() {
        // given - a schedule with two versions and two rules created at different times
        Schedule schedule = createSchedule("Test Schedule", "");
        Version version1 = createVersion(schedule.getId());
        createRule(schedule.getId(), version1.getId(), WEEKDAYS_ONLY, true);

        // ensure created at is different
        try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }

        Version version2 = createVersion(schedule.getId());
        Rule latestRule = createRule(schedule.getId(), version2.getId(), ALL_DAYS, true);

        // when - the latest rule is requested
        Optional<Rule> foundRule = ruleRepository.findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(schedule.getId());

        // then - the latest rule is found
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getId()).isEqualTo(latestRule.getId());
    }

    private Schedule createSchedule(String name, String description) {
        Schedule schedule = new Schedule(name, description);
        return scheduleRepository.save(schedule);
    }

    private Version createVersion(UUID scheduleId) {
        Version version = Version.builder().scheduleId(scheduleId).build();
        return versionRepository.save(version);
    }

    private Rule createRule(UUID scheduleId, UUID versionId, Rule.RuleType ruleType, boolean active) {
        return createRule(scheduleId, versionId, ruleType, active, LocalDate.now());
    }

    private Rule createRule(UUID scheduleId, UUID versionId, Rule.RuleType ruleType, boolean active, LocalDate effectiveFrom) {
        Rule rule = Rule.builder()
                .scheduleId(scheduleId)
                .versionId(versionId)
                .ruleType(ruleType)
                .active(active)
                .effectiveFrom(effectiveFrom)
                .build();
        return ruleRepository.saveAndFlush(rule);
    }
}
