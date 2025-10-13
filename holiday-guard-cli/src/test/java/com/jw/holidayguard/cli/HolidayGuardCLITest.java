package com.jw.holidayguard.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the CLI application.
 * Tests the full command-line interface including argument parsing and output.
 */
class HolidayGuardCLITest {

    @Test
    void cli_shouldShowHelpWhenRequested() {
        // Given: CLI with --help flag
        HolidayGuardCLI cli = new HolidayGuardCLI();
        CommandLine cmd = new CommandLine(cli);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        // When: Executing with --help
        int exitCode = cmd.execute("--help");

        // Then: Shows help and exits successfully
        assertThat(exitCode).isEqualTo(0);
        String output = out.toString();
        assertThat(output).contains("Usage:");
        assertThat(output).contains("holiday-guard");
        assertThat(output).contains("--help");
    }

    @Test
    void cli_shouldReturnExitCode0ForScheduleThatShouldRun(@TempDir Path tempDir) throws IOException {
        // Given: Config with weekdays-only schedule on a Monday
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Querying Monday (should run)
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Test Schedule";
        cli.dateInput = "2025-10-13"; // Monday
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Exit code is 0 (run)
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void cli_shouldReturnExitCode1ForScheduleThatShouldSkip(@TempDir Path tempDir) throws IOException {
        // Given: Config with weekdays-only schedule on a Saturday
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Querying Saturday (should skip)
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Test Schedule";
        cli.dateInput = "2025-10-18"; // Saturday
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Exit code is 1 (skip)
        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void cli_shouldReturnExitCode2ForMissingConfigFile() {
        // Given: Non-existent config file
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Test Schedule";
        cli.configFile = new File("/does/not/exist.json");
        cli.quiet = true;

        // When: Executing
        int exitCode = cli.call();

        // Then: Exit code is 2 (error)
        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    void cli_shouldReturnExitCode2ForNonExistentSchedule(@TempDir Path tempDir) throws IOException {
        // Given: Config without target schedule
        String json = """
            {
              "schedules": [
                {
                  "name": "Different Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Querying non-existent schedule
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Does Not Exist";
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Exit code is 2 (error)
        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    void cli_shouldOutputTextFormatByDefault(@TempDir Path tempDir) throws IOException {
        // Given: Valid config
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // Capture output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            // When: Executing with text format (default)
            HolidayGuardCLI cli = new HolidayGuardCLI();
            cli.scheduleName = "Test Schedule";
            cli.dateInput = "2025-10-13";
            cli.configFile = configFile;
            cli.format = HolidayGuardCLI.OutputFormat.TEXT;

            cli.call();

            // Then: Output is human-readable text
            String output = out.toString();
            assertThat(output).contains("Schedule:");
            assertThat(output).contains("Date:");
            assertThat(output).contains("Status:");
            assertThat(output).contains("Result:");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void cli_shouldOutputJsonFormat(@TempDir Path tempDir) throws IOException {
        // Given: Valid config
        String json = """
            {
              "schedules": [
                {
                  "name": "Test Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // Capture output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            // When: Executing with JSON format
            HolidayGuardCLI cli = new HolidayGuardCLI();
            cli.scheduleName = "Test Schedule";
            cli.dateInput = "2025-10-13";
            cli.configFile = configFile;
            cli.format = HolidayGuardCLI.OutputFormat.JSON;

            cli.call();

            // Then: Output is valid JSON
            String output = out.toString().trim();
            assertThat(output).startsWith("{");
            assertThat(output).endsWith("}");
            assertThat(output).contains("\"schedule\":");
            assertThat(output).contains("\"date\":");
            assertThat(output).contains("\"shouldRun\":");
            assertThat(output).contains("\"status\":");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void cli_shouldHandleDeviations(@TempDir Path tempDir) throws IOException {
        // Given: Schedule with FORCE_SKIP deviation on Christmas
        String json = """
            {
              "schedules": [
                {
                  "name": "Payroll",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": [
                    {
                      "date": "2025-12-25",
                      "action": "FORCE_SKIP",
                      "reason": "Christmas Day"
                    }
                  ]
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Querying Christmas (Thursday, but has FORCE_SKIP)
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Payroll";
        cli.dateInput = "2025-12-25";
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Exit code is 1 (skip due to deviation)
        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void cli_shouldHandleForceRunDeviation(@TempDir Path tempDir) throws IOException {
        // Given: Schedule with FORCE_RUN on weekend
        String json = """
            {
              "schedules": [
                {
                  "name": "Emergency",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": [
                    {
                      "date": "2025-10-18",
                      "action": "FORCE_RUN",
                      "reason": "Emergency processing"
                    }
                  ]
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Querying Saturday (weekend, but has FORCE_RUN)
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Emergency";
        cli.dateInput = "2025-10-18";
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Exit code is 0 (run due to deviation)
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void cli_shouldDefaultToToday(@TempDir Path tempDir) throws IOException {
        // Given: Config without date specified
        String json = """
            {
              "schedules": [
                {
                  "name": "Test",
                  "rule": {"ruleType": "ALL_DAYS"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Not specifying date (defaults to "today")
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Test";
        cli.dateInput = "today"; // Explicit default
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Executes successfully
        assertThat(exitCode).isIn(0, 1); // Depends on today's date
    }

    @Test
    void cli_shouldBeScheduleNameCaseInsensitive(@TempDir Path tempDir) throws IOException {
        // Given: Schedule with specific casing
        String json = """
            {
              "schedules": [
                {
                  "name": "Payroll Schedule",
                  "rule": {"ruleType": "WEEKDAYS_ONLY"},
                  "deviations": []
                }
              ]
            }
            """;

        File configFile = createConfigFile(tempDir, json);

        // When: Querying with different casing
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "payroll schedule"; // lowercase
        cli.dateInput = "2025-10-13";
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // Then: Finds schedule successfully
        assertThat(exitCode).isEqualTo(0);
    }

    // Helper method to create config files
    private File createConfigFile(Path tempDir, String json) throws IOException {
        File configFile = tempDir.resolve("test-config.json").toFile();
        Files.writeString(configFile.toPath(), json);
        return configFile;
    }
}
