package com.jw.holidayguard.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CLIConfigLoaderTest {

    private final CLIConfigLoader loader = new CLIConfigLoader();

    @Test
    void loadConfig_shouldParseValidJsonFile(@TempDir Path tempDir) throws IOException {
        // given - Valid JSON config file
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "description": "Test description",
                  "rule": {
                    "ruleType": "WEEKDAYS_ONLY"
                  },
                  "deviations": [
                    {
                      "date": "2025-12-25",
                      "action": "FORCE_SKIP",
                      "reason": "Christmas"
                    }
                  ]
                }
              ]
            }
            """;

        File configFile = tempDir.resolve("test-config.json").toFile();
        Files.writeString(configFile.toPath(), json);

        // when - Loading config
        CLIConfig config = loader.loadConfig(configFile);

        // then - Config is parsed correctly
        assertThat(config.getSchedules()).hasSize(1);

        CLIConfig.ScheduleConfig schedule = config.getSchedules().getFirst();
        assertThat(schedule.getName()).isEqualTo("Test Schedule");
        assertThat(schedule.getDescription()).isEqualTo("Test description");
        assertThat(schedule.getRule().getRuleType()).isEqualTo("WEEKDAYS_ONLY");
        assertThat(schedule.getDeviations()).hasSize(1);

        CLIConfig.DeviationConfig deviation = schedule.getDeviations().getFirst();
        assertThat(deviation.getDate()).isEqualTo(LocalDate.of(2025, 12, 25));
        assertThat(deviation.getAction()).isEqualTo("FORCE_SKIP");
        assertThat(deviation.getReason()).isEqualTo("Christmas");
    }

    @Test
    void loadConfig_shouldParseMultipleSchedules(@TempDir Path tempDir) throws IOException {
        // given - Config with multiple schedules
        String json = """
            {
              "schedules": [
                {
                  "name": "Schedule 1",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                },
                {
                  "name": "Schedule 2",
                  "rule": {"ruleType": "CRON_EXPRESSION", "ruleConfig": "0 0 * * *"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = tempDir.resolve("multi-config.json").toFile();
        Files.writeString(configFile.toPath(), json);

        // when - Loading config
        CLIConfig config = loader.loadConfig(configFile);

        // then - Both schedules are loaded
        assertThat(config.getSchedules()).hasSize(2);
        assertThat(config.getScheduleNames()).containsExactly("Schedule 1", "Schedule 2");
    }

    @Test
    void loadConfig_shouldHandleEmptyDeviations(@TempDir Path tempDir) throws IOException {
        // given - Schedule without deviations
        String json = """
            {
              "schedules": [
                {
                  "name": "Simple Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"}
                }
              ]
            }
            """;

        File configFile = tempDir.resolve("no-deviations.json").toFile();
        Files.writeString(configFile.toPath(), json);

        // when - Loading config
        CLIConfig config = loader.loadConfig(configFile);

        // then - Schedule has empty deviations list
        assertThat(config.getSchedules().getFirst().getDeviations()).isEmpty();
    }

    @Test
    void loadConfig_shouldThrowExceptionForInvalidJson(@TempDir Path tempDir) throws IOException {
        // given - Invalid JSON
        String invalidJson = "{ invalid json }";
        File configFile = tempDir.resolve("invalid.json").toFile();
        Files.writeString(configFile.toPath(), invalidJson);

        // When/Then: Exception thrown
        assertThatThrownBy(() -> loader.loadConfig(configFile))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Failed to parse configuration file");
    }

    @Test
    void loadConfig_shouldThrowExceptionForNonExistentFile() {
        // given - Non-existent file
        File nonExistent = new File("/does/not/exist.json");

        // When/Then: Exception thrown
        assertThatThrownBy(() -> loader.loadConfig(nonExistent))
            .isInstanceOf(IOException.class);
    }

    @Test
    void findSchedule_shouldReturnScheduleByName(@TempDir Path tempDir) throws IOException {
        // given - Config with multiple schedules
        String json = """
            {
              "schedules": [
                {"name": "Alpha", "rule": {"ruleType": "WEEKDAYS_ONLY"}, "deviations": []},
                {"name": "Beta", "rule": {"ruleType": "WEEKDAYS_ONLY"}, "deviations": []},
                {"name": "Gamma", "rule": {"ruleType": "WEEKDAYS_ONLY"}, "deviations": []}
              ]
            }
            """;

        File configFile = tempDir.resolve("multi.json").toFile();
        Files.writeString(configFile.toPath(), json);
        CLIConfig config = loader.loadConfig(configFile);

        // when - Finding schedule
        CLIConfig.ScheduleConfig found = config.findSchedule("Beta");

        // then - Correct schedule returned
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Beta");
    }

    @Test
    void findSchedule_shouldBeCaseInsensitive(@TempDir Path tempDir) throws IOException {

        // given - schedule with specific casing
        String json = """
            {
              "schedules": [
                {"name": "Payroll Schedule", "rule": {"ruleType": "WEEKDAYS_ONLY"}, "deviations": []}
              ]
            }
            """;

        File configFile = tempDir.resolve("case-test.json").toFile();
        Files.writeString(configFile.toPath(), json);
        CLIConfig config = loader.loadConfig(configFile);

        // when - searching with different case
        CLIConfig.ScheduleConfig found1 = config.findSchedule("payroll schedule");
        CLIConfig.ScheduleConfig found2 = config.findSchedule("PAYROLL SCHEDULE");
        CLIConfig.ScheduleConfig found3 = config.findSchedule("Payroll Schedule");

        // then - all are found
        assertThat(found1).isNotNull();
        assertThat(found2).isNotNull();
        assertThat(found3).isNotNull();
    }

    @Test
    void findSchedule_shouldReturnNullForNonExistent(@TempDir Path tempDir) throws IOException {

        // given - config without target schedule
        String json = """
            {
              "schedules": [
                {"name": "Schedule 1", "rule": {"ruleType": "WEEKDAYS_ONLY"}, "deviations": []}
              ]
            }
            """;

        File configFile = tempDir.resolve("test.json").toFile();
        Files.writeString(configFile.toPath(), json);
        CLIConfig config = loader.loadConfig(configFile);

        // when - searching for non-existent schedule
        CLIConfig.ScheduleConfig notFound = config.findSchedule("Does Not Exist");

        // then - nothing is found
        assertThat(notFound).isNull();
    }

    // --- Baseline tests for Jackson migration (Phase 0c) ---

    @Test
    void loadConfig_shouldThrowForInvalidDateFormat(@TempDir Path tempDir) throws IOException {
        // given - deviation with an invalid month (13 is out of range)
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": [
                    {
                      "date": "2025-13-01",
                      "action": "FORCE_SKIP",
                      "reason": "Invalid date"
                    }
                  ]
                }
              ]
            }
            """;

        File configFile = tempDir.resolve("bad-date.json").toFile();
        Files.writeString(configFile.toPath(), json);

        // when / then - loader should reject the invalid date and throw IOException
        assertThatThrownBy(() -> loader.loadConfig(configFile))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Failed to parse configuration file");
    }

    @Test
    void loadConfig_shouldHandleNullDate(@TempDir Path tempDir) throws IOException {
        // given - deviation with an explicit null date field
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": [
                    {
                      "date": null,
                      "action": "FORCE_SKIP",
                      "reason": "Null date"
                    }
                  ]
                }
              ]
            }
            """;

        File configFile = tempDir.resolve("null-date.json").toFile();
        Files.writeString(configFile.toPath(), json);

        // when
        CLIConfig config = loader.loadConfig(configFile);

        // then - parsed successfully, date field is null
        CLIConfig.DeviationConfig deviation = config.getSchedules().getFirst().getDeviations().getFirst();
        assertThat(deviation.getDate()).isNull();
    }

    @Test
    void loadConfig_dateFormat_shouldMatchLocalDateParse(@TempDir Path tempDir) throws IOException {
        // given - a known date in ISO-8601 format
        String isoDate = "2025-12-25";
        String json = """
            {
              "schedules": [
                {
                  "name": "Format Contract",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": [
                    {
                      "date": "%s",
                      "action": "FORCE_SKIP",
                      "reason": "Christmas"
                    }
                  ]
                }
              ]
            }
            """.formatted(isoDate);

        File configFile = tempDir.resolve("format-contract.json").toFile();
        Files.writeString(configFile.toPath(), json);

        // when
        CLIConfig config = loader.loadConfig(configFile);
        LocalDate parsed = config.getSchedules().getFirst().getDeviations().getFirst().getDate();

        // then - Jackson-parsed date equals LocalDate.parse() for the same string
        // This contract must hold after Jackson 2→3 migration
        assertThat(parsed)
            .as("Jackson-deserialized LocalDate must match LocalDate.parse() for the same ISO string")
            .isEqualTo(LocalDate.parse(isoDate));
    }
}
