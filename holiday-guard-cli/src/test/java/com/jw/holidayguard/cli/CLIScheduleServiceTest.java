package com.jw.holidayguard.cli;

import com.jw.holidayguard.domain.Calendar;
import com.jw.holidayguard.domain.RunStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CLIScheduleServiceTest {

    private CLIScheduleService service;

    @BeforeEach
    void setUp() {
        service = new CLIScheduleService();
    }

    @Test
    void buildCalendar_shouldCreateCalendarFromWeekdaysOnlySchedule() {
        // given - Weekdays-only schedule config
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test Schedule");
        config.setDescription("Weekdays only");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);
        config.setDeviations(new ArrayList<>());

        // when - Building calendar
        Calendar calendar = service.buildCalendar(config);

        // then - Calendar evaluates weekdays correctly
        LocalDate monday = LocalDate.of(2025, 10, 13); // Monday
        LocalDate saturday = LocalDate.of(2025, 10, 18); // Saturday
        LocalDate sunday = LocalDate.of(2025, 10, 19); // Sunday

        assertThat(calendar.shouldRun(monday)).isTrue();
        assertThat(calendar.shouldRun(saturday)).isFalse();
        assertThat(calendar.shouldRun(sunday)).isFalse();
    }

    @Test
    void buildCalendar_shouldApplyDeviations() {
        // given - Schedule with FORCE_SKIP deviation
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test Schedule");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);

        LocalDate christmas = LocalDate.of(2025, 12, 25); // Thursday
        CLIConfig.DeviationConfig deviation = new CLIConfig.DeviationConfig();
        deviation.setDate(christmas);
        deviation.setAction("FORCE_SKIP");
        deviation.setReason("Christmas Day");

        config.setDeviations(List.of(deviation));

        // when - Building calendar
        Calendar calendar = service.buildCalendar(config);

        // then - Deviation overrides weekday rule
        assertThat(calendar.shouldRun(christmas)).isFalse();
    }

    @Test
    void buildCalendar_shouldHandleForceRunDeviation() {
        // given - Schedule with FORCE_RUN on weekend
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test Schedule");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);

        LocalDate saturday = LocalDate.of(2025, 10, 18); // Saturday
        CLIConfig.DeviationConfig deviation = new CLIConfig.DeviationConfig();
        deviation.setDate(saturday);
        deviation.setAction("FORCE_RUN");
        deviation.setReason("Emergency processing");

        config.setDeviations(List.of(deviation));

        // when - Building calendar
        Calendar calendar = service.buildCalendar(config);

        // then - Weekend day runs due to FORCE_RUN
        assertThat(calendar.shouldRun(saturday)).isTrue();
    }

    @Test
    void buildCalendar_shouldHandleCronExpression() {
        // given - Daily cron schedule
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Daily Backup");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("CRON_EXPRESSION");
        rule.setRuleConfig("0 0 0 * * *"); // Every day at midnight (6 fields: sec min hour day month dow)
        config.setRule(rule);
        config.setDeviations(new ArrayList<>());

        // when - Building calendar
        Calendar calendar = service.buildCalendar(config);

        // then - Should run every day
        LocalDate weekday = LocalDate.of(2025, 10, 13);
        LocalDate weekend = LocalDate.of(2025, 10, 18);

        assertThat(calendar.shouldRun(weekday)).isTrue();
        assertThat(calendar.shouldRun(weekend)).isTrue();
    }

    @Test
    void buildCalendar_shouldHandleNoDaysRule() {
        // given - NO_DAYS schedule
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Disabled Schedule");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("NO_DAYS");
        config.setRule(rule);
        config.setDeviations(new ArrayList<>());

        // when - Building calendar
        Calendar calendar = service.buildCalendar(config);

        // then - Never runs
        LocalDate anyDay = LocalDate.of(2025, 10, 13);
        assertThat(calendar.shouldRun(anyDay)).isFalse();
    }

    @Test
    void determineRunStatus_shouldReturnRunForWeekday() {
        // given - Weekdays-only schedule
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);
        config.setDeviations(new ArrayList<>());

        Calendar calendar = service.buildCalendar(config);
        LocalDate monday = LocalDate.of(2025, 10, 13);

        // when - Determining status
        RunStatus status = service.determineRunStatus(calendar, config, monday);

        // then - Status is RUN
        assertThat(status).isEqualTo(RunStatus.RUN);
    }

    @Test
    void determineRunStatus_shouldReturnSkipForWeekend() {
        // given - Weekdays-only schedule
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);
        config.setDeviations(new ArrayList<>());

        Calendar calendar = service.buildCalendar(config);
        LocalDate saturday = LocalDate.of(2025, 10, 18);

        // when - Determining status
        RunStatus status = service.determineRunStatus(calendar, config, saturday);

        // then - Status is SKIP
        assertThat(status).isEqualTo(RunStatus.SKIP);
    }

    @Test
    void determineRunStatus_shouldReturnForceSkipForDeviation() {
        // given - Schedule with FORCE_SKIP deviation
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);

        LocalDate christmas = LocalDate.of(2025, 12, 25);
        CLIConfig.DeviationConfig deviation = new CLIConfig.DeviationConfig();
        deviation.setDate(christmas);
        deviation.setAction("FORCE_SKIP");
        deviation.setReason("Holiday");

        config.setDeviations(List.of(deviation));

        Calendar calendar = service.buildCalendar(config);

        // when - Determining status
        RunStatus status = service.determineRunStatus(calendar, config, christmas);

        // then - Status is FORCE_SKIP
        assertThat(status).isEqualTo(RunStatus.FORCE_SKIP);
    }

    @Test
    void determineRunStatus_shouldReturnForceRunForDeviation() {
        // given - Schedule with FORCE_RUN on weekend
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);

        LocalDate saturday = LocalDate.of(2025, 10, 18);
        CLIConfig.DeviationConfig deviation = new CLIConfig.DeviationConfig();
        deviation.setDate(saturday);
        deviation.setAction("FORCE_RUN");
        deviation.setReason("Emergency");

        config.setDeviations(List.of(deviation));

        Calendar calendar = service.buildCalendar(config);

        // when - Determining status
        RunStatus status = service.determineRunStatus(calendar, config, saturday);

        // then - Status is FORCE_RUN
        assertThat(status).isEqualTo(RunStatus.FORCE_RUN);
    }

    @Test
    void determineRunStatus_shouldHandleMultipleDeviations() {
        // given - Schedule with multiple deviations
        CLIConfig.ScheduleConfig config = new CLIConfig.ScheduleConfig();
        config.setName("Test");

        CLIConfig.RuleConfig rule = new CLIConfig.RuleConfig();
        rule.setRuleType("WEEKDAYS_ONLY");
        config.setRule(rule);

        LocalDate date1 = LocalDate.of(2025, 12, 25);
        LocalDate date2 = LocalDate.of(2025, 12, 26);

        CLIConfig.DeviationConfig dev1 = new CLIConfig.DeviationConfig();
        dev1.setDate(date1);
        dev1.setAction("FORCE_SKIP");
        dev1.setReason("Christmas");

        CLIConfig.DeviationConfig dev2 = new CLIConfig.DeviationConfig();
        dev2.setDate(date2);
        dev2.setAction("FORCE_SKIP");
        dev2.setReason("Boxing Day");

        config.setDeviations(List.of(dev1, dev2));

        Calendar calendar = service.buildCalendar(config);

        // when - Checking both dates
        RunStatus status1 = service.determineRunStatus(calendar, config, date1);
        RunStatus status2 = service.determineRunStatus(calendar, config, date2);

        // then - Both have correct deviation status
        assertThat(status1).isEqualTo(RunStatus.FORCE_SKIP);
        assertThat(status2).isEqualTo(RunStatus.FORCE_SKIP);
    }
}
