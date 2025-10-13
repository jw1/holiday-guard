package com.jw.holidayguard.cli;

import com.jw.holidayguard.domain.Calendar;
import com.jw.holidayguard.domain.RunStatus;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.Callable;

/**
 * Command-line interface for Holiday Guard schedule queries.
 *
 * <p>Usage examples:
 * <pre>
 * # Query if "Payroll Schedule" should run today
 * java -jar holiday-guard-cli.jar "Payroll Schedule"
 *
 * # Query specific date
 * java -jar holiday-guard-cli.jar "Payroll Schedule" --date 2025-12-25
 *
 * # Use custom config file
 * java -jar holiday-guard-cli.jar "ACH Processing" --config /path/to/config.json
 *
 * # Quiet mode (only exit code)
 * java -jar holiday-guard-cli.jar "Payroll Schedule" --quiet
 * </pre>
 *
 * <p>Exit codes:
 * <ul>
 *   <li>0 = Schedule should run</li>
 *   <li>1 = Schedule should not run</li>
 *   <li>2 = Error (schedule not found, invalid config, etc.)</li>
 * </ul>
 */
@Command(
    name = "holiday-guard",
    mixinStandardHelpOptions = true,
    version = "Holiday Guard CLI 0.0.1",
    description = "Query whether a schedule should run on a given date"
)
public class HolidayGuardCLI implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "Schedule name (e.g., 'Payroll Schedule')"
    )
    String scheduleName; // Package-private for testing

    @Option(
        names = {"-d", "--date"},
        description = "Query date in ISO format (default: today). Examples: 2025-12-25, today"
    )
    String dateInput = "today"; // Package-private for testing

    @Option(
        names = {"-c", "--config"},
        description = "Path to JSON configuration file (default: ./schedules.json)"
    )
    File configFile = new File("schedules.json"); // Package-private for testing

    @Option(
        names = {"-q", "--quiet"},
        description = "Quiet mode - suppress output, only use exit code"
    )
    boolean quiet = false; // Package-private for testing

    @Option(
        names = {"-v", "--verbose"},
        description = "Verbose output - show detailed reasoning"
    )
    boolean verbose = false; // Package-private for testing

    @Option(
        names = {"--format"},
        description = "Output format: text, json (default: text)"
    )
    OutputFormat format = OutputFormat.TEXT; // Package-private for testing

    private final CLIConfigLoader configLoader;
    private final CLIScheduleService scheduleService;

    public HolidayGuardCLI() {
        this.configLoader = new CLIConfigLoader();
        this.scheduleService = new CLIScheduleService();
    }

    // Constructor for testing
    HolidayGuardCLI(CLIConfigLoader configLoader, CLIScheduleService scheduleService) {
        this.configLoader = configLoader;
        this.scheduleService = scheduleService;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new HolidayGuardCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            // Parse date
            LocalDate queryDate = parseDate(dateInput);

            // Load configuration
            if (!configFile.exists()) {
                System.err.println("Error: Configuration file not found: " + configFile.getAbsolutePath());
                System.err.println("Create a schedules.json file or specify --config path");
                return 2;
            }

            CLIConfig config = configLoader.loadConfig(configFile);

            // Find schedule
            CLIConfig.ScheduleConfig scheduleConfig = config.findSchedule(scheduleName);
            if (scheduleConfig == null) {
                System.err.println("Error: Schedule not found: " + scheduleName);
                System.err.println("Available schedules: " + config.getScheduleNames());
                return 2;
            }

            // Build calendar and query shouldRun
            Calendar calendar = scheduleService.buildCalendar(scheduleConfig);
            boolean shouldRun = calendar.shouldRun(queryDate);
            RunStatus status = scheduleService.determineRunStatus(calendar, scheduleConfig, queryDate);

            // Output result
            if (!quiet) {
                outputResult(scheduleName, queryDate, shouldRun, status, scheduleConfig);
            }

            // Return exit code: 0 = run, 1 = skip
            return shouldRun ? 0 : 1;

        } catch (Exception e) {
            if (!quiet) {
                System.err.println("Error: " + e.getMessage());
                if (verbose) {
                    e.printStackTrace(System.err);
                }
            }
            return 2;
        }
    }

    private LocalDate parseDate(String input) {
        if ("today".equalsIgnoreCase(input)) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(input);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + input + ". Use ISO format (YYYY-MM-DD) or 'today'");
        }
    }

    private void outputResult(String schedule, LocalDate date, boolean shouldRun, RunStatus status, CLIConfig.ScheduleConfig config) {
        if (format == OutputFormat.JSON) {
            outputJson(schedule, date, shouldRun, status, config);
        } else {
            outputText(schedule, date, shouldRun, status, config);
        }
    }

    private void outputText(String schedule, LocalDate date, boolean shouldRun, RunStatus status, CLIConfig.ScheduleConfig config) {
        System.out.println("Schedule: " + schedule);
        System.out.println("Date:     " + date);
        System.out.println("Status:   " + status);
        System.out.println("Result:   " + (shouldRun ? "RUN" : "SKIP"));

        if (verbose) {
            System.out.println("\nDetails:");
            System.out.println("  Rule Type: " + config.getRule().getRuleType());
            System.out.println("  Rule Config: " + (config.getRule().getRuleConfig() != null ? config.getRule().getRuleConfig() : "N/A"));
            if (config.getDeviations() != null && !config.getDeviations().isEmpty()) {
                System.out.println("  Deviations: " + config.getDeviations().size() + " configured");
            }
        }
    }

    private void outputJson(String schedule, LocalDate date, boolean shouldRun, RunStatus status, CLIConfig.ScheduleConfig config) {
        System.out.printf("{\"schedule\":\"%s\",\"date\":\"%s\",\"shouldRun\":%b,\"status\":\"%s\"}%n",
            schedule, date, shouldRun, status);
    }

    enum OutputFormat {
        TEXT, JSON
    }
}
