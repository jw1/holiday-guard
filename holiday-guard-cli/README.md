# Holiday Guard CLI

Command-line interface for querying Holiday Guard schedules. Perfect for shell scripts, cron jobs, and automation workflows.

## Features

- ✅ **Fast startup** - Minimal dependencies (no web server)
- ✅ **JSON configuration** - Define schedules in a simple JSON file
- ✅ **Exit codes** - Shell-friendly: 0 = run, 1 = skip, 2 = error
- ✅ **Multiple output formats** - Text (human-readable) or JSON (machine-readable)
- ✅ **Date queries** - Check "today" or any specific date
- ✅ **All rule types supported** - Weekdays, cron, Federal Reserve, custom dates, etc.

## Quick Start

### 1. Build the JAR

```bash
cd holiday-guard
./mvnw clean package -pl holiday-guard-cli -am
```

This creates an executable JAR at:
```
holiday-guard-cli/target/holiday-guard-cli-0.0.1-SNAPSHOT.jar
```

### 2. Create a configuration file

Create `schedules.json` in your working directory:

```json
{
  "schedules": [
    {
      "name": "Payroll Schedule",
      "description": "Runs on weekdays except holidays",
      "rule": {
        "ruleType": "WEEKDAYS_ONLY"
      },
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
```

See [schedules-example.json](src/main/resources/schedules-example.json) for more examples.

### 3. Query a schedule

```bash
# Should my payroll job run today?
java -jar holiday-guard-cli.jar "Payroll Schedule"

# Check a specific date
java -jar holiday-guard-cli.jar "Payroll Schedule" --date 2025-12-25

# Use in a script
if java -jar holiday-guard-cli.jar "Payroll Schedule" --quiet; then
    echo "Running payroll..."
    ./run-payroll.sh
else
    echo "Skipping payroll today"
fi
```

## Usage

```
Usage: holiday-guard [-hqvV] [--format=<format>] [-c=<configFile>]
                     [-d=<dateInput>] <scheduleName>

Query whether a schedule should run on a given date

Parameters:
  <scheduleName>        Schedule name (e.g., 'Payroll Schedule')

Options:
  -c, --config=<configFile>
                        Path to JSON configuration file (default: ./schedules.json)
  -d, --date=<dateInput>
                        Query date in ISO format (default: today)
                        Examples: 2025-12-25, today
      --format=<format> Output format: text, json (default: text)
  -h, --help            Show this help message and exit.
  -q, --quiet           Quiet mode - suppress output, only use exit code
  -v, --verbose         Verbose output - show detailed reasoning
  -V, --version         Print version information and exit.
```

## Exit Codes

- **0** = Schedule should run
- **1** = Schedule should not run
- **2** = Error (schedule not found, invalid config, etc.)

## Configuration Format

```json
{
  "schedules": [
    {
      "name": "Schedule Name",
      "description": "Optional description",
      "rule": {
        "ruleType": "RULE_TYPE",
        "ruleConfig": "optional config string"
      },
      "deviations": [
        {
          "date": "2025-12-25",
          "action": "FORCE_SKIP",
          "reason": "Holiday"
        }
      ]
    }
  ]
}
```

### Supported Rule Types

| Rule Type | Description | ruleConfig Example |
|-----------|-------------|-------------------|
| `WEEKDAYS_ONLY` | Monday-Friday only | N/A |
| `CRON_EXPRESSION` | Standard cron syntax | `"0 0 * * *"` (daily at midnight) |
| `US_FEDERAL_RESERVE_BUSINESS_DAYS` | Federal Reserve banking days | N/A |
| `FIRST_BUSINESS_DAY_OF_MONTH` | First weekday of month | N/A |
| `LAST_BUSINESS_DAY_OF_MONTH` | Last weekday of month | N/A |
| `SPECIFIC_DATES` | Comma-separated dates | `"2025-01-15,2025-02-15"` |
| `NO_DAYS` | Never runs | N/A |

### Deviation Actions

- `FORCE_RUN` - Override rule to force execution
- `FORCE_SKIP` - Override rule to prevent execution

## Examples

### Basic Usage

```bash
# Query today
java -jar holiday-guard-cli.jar "Payroll Schedule"

# Query specific date
java -jar holiday-guard-cli.jar "Payroll Schedule" --date 2025-07-04
```

### Shell Script Integration

```bash
#!/bin/bash
# run-if-scheduled.sh

SCHEDULE_NAME="Nightly Backup"
CLI_JAR="holiday-guard-cli.jar"

if java -jar "$CLI_JAR" "$SCHEDULE_NAME" --quiet; then
    echo "✓ Running backup..."
    ./backup.sh
    exit $?
else
    echo "⊗ Backup skipped today"
    exit 0
fi
```

### Cron Integration

```bash
# Run payroll at 8am if schedule says to run
0 8 * * * cd /opt/payroll && java -jar holiday-guard-cli.jar "Payroll Schedule" -q && ./process-payroll.sh
```

### JSON Output

```bash
# Machine-readable output
java -jar holiday-guard-cli.jar "Payroll Schedule" --format json
# {"schedule":"Payroll Schedule","date":"2025-10-13","shouldRun":true,"status":"RUN"}

# Parse with jq
SHOULD_RUN=$(java -jar holiday-guard-cli.jar "Payroll" --format json | jq -r .shouldRun)
```

### Custom Config File

```bash
# Use a different config file
java -jar holiday-guard-cli.jar "ACH Processing" --config /etc/schedules/prod.json
```

### Verbose Output

```bash
# Show detailed information
java -jar holiday-guard-cli.jar "Payroll Schedule" --verbose

# Output:
# Schedule: Payroll Schedule
# Date:     2025-10-13
# Status:   RUN
# Result:   RUN
#
# Details:
#   Rule Type: WEEKDAYS_ONLY
#   Rule Config: N/A
#   Deviations: 3 configured
```

## Advanced Patterns

### Multi-Schedule Orchestration

```bash
#!/bin/bash
# Check multiple schedules and run corresponding jobs

schedules=("Payroll" "ACH Processing" "Reports")
jobs=("./payroll.sh" "./ach.sh" "./reports.sh")

for i in "${!schedules[@]}"; do
    if java -jar holiday-guard-cli.jar "${schedules[$i]}" -q; then
        echo "Running ${schedules[$i]}..."
        ${jobs[$i]}
    fi
done
```

### Date Range Testing

```bash
#!/bin/bash
# Test which days in December the schedule will run

for day in {1..31}; do
    date="2025-12-$(printf "%02d" $day)"
    if java -jar holiday-guard-cli.jar "Payroll" --date "$date" -q; then
        echo "$date: RUN"
    else
        echo "$date: SKIP"
    fi
done
```

### Slack/Email Notifications

```bash
#!/bin/bash
# Send notification if schedule skips today

if ! java -jar holiday-guard-cli.jar "Critical Job" -q; then
    curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
        -d '{"text":"⚠️ Critical Job skipped today"}'
fi
```

## Building from Source

```bash
# Build just the CLI module
./mvnw clean package -pl holiday-guard-cli -am

# Build with tests
./mvnw clean verify -pl holiday-guard-cli -am

# Skip tests for faster builds
./mvnw clean package -pl holiday-guard-cli -am -DskipTests
```

## Future: GraalVM Native Image

For ultra-fast startup (~50ms), we plan to add GraalVM native-image support:

```bash
# Future command (not yet implemented)
./holiday-guard "Payroll Schedule"  # Native binary, instant startup
```

This will be in a separate `holiday-guard-cli-native` module to avoid affecting the standard JVM build.

## Troubleshooting

### "Configuration file not found"

Make sure `schedules.json` exists in your current directory, or specify `--config` with the full path.

### "Schedule not found"

Check that the schedule name matches exactly (case-insensitive). Use `--verbose` to see available schedules.

### Wrong exit code in scripts

Make sure to use `--quiet` mode in scripts to suppress output and only rely on exit codes.

## See Also

- [Main README](../README.md) - Web service documentation
- [CLAUDE.md](../CLAUDE.md) - Architecture and design patterns
- [Example Config](src/main/resources/schedules-example.json) - Full configuration examples
