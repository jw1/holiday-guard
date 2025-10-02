import { CronBuilderState, DayOfMonthMode } from '../types/cron';

/**
 * Compress an array of numbers into cron range/list notation
 * Examples:
 *  [1,2,3,4,5] → "1-5"
 *  [1,3,5,7] → "1,3,5,7"
 *  [1,2,3,10,11,12] → "1-3,10-12"
 */
export function compressToRangesAndLists(values: number[]): string {
    if (values.length === 0) return '*';
    if (values.length === 1) return String(values[0]);

    const sorted = [...values].sort((a, b) => a - b);
    const ranges: string[] = [];
    let rangeStart = sorted[0];
    let rangeEnd = sorted[0];

    for (let i = 1; i < sorted.length; i++) {
        if (sorted[i] === rangeEnd + 1) {
            // Continue the range
            rangeEnd = sorted[i];
        } else {
            // End current range and start new one
            if (rangeEnd - rangeStart >= 2) {
                // Range of 3+ numbers: use range notation
                ranges.push(`${rangeStart}-${rangeEnd}`);
            } else if (rangeEnd === rangeStart) {
                // Single number
                ranges.push(String(rangeStart));
            } else {
                // Range of 2 numbers: use list notation
                ranges.push(String(rangeStart));
                ranges.push(String(rangeEnd));
            }
            rangeStart = sorted[i];
            rangeEnd = sorted[i];
        }
    }

    // Add final range
    if (rangeEnd - rangeStart >= 2) {
        ranges.push(`${rangeStart}-${rangeEnd}`);
    } else if (rangeEnd === rangeStart) {
        ranges.push(String(rangeStart));
    } else {
        ranges.push(String(rangeStart));
        ranges.push(String(rangeEnd));
    }

    return ranges.join(',');
}

/**
 * Expand cron field (range/list notation) into array of numbers
 * Examples:
 *  "1-5" → [1,2,3,4,5]
 *  "1,3,5" → [1,3,5]
 *  "1-3,10-12" → [1,2,3,10,11,12]
 *  "*" → [] (empty array means "all")
 */
export function expandCronField(field: string, min: number, max: number): number[] {
    if (field === '*' || field === '?') return [];

    const values = new Set<number>();

    const parts = field.split(',');
    for (const part of parts) {
        if (part.includes('-')) {
            const [start, end] = part.split('-').map(Number);
            for (let i = start; i <= end; i++) {
                if (i >= min && i <= max) {
                    values.add(i);
                }
            }
        } else {
            const num = Number(part);
            if (!isNaN(num) && num >= min && num <= max) {
                values.add(num);
            }
        }
    }

    return Array.from(values).sort((a, b) => a - b);
}

/**
 * Parse a full 6-field cron expression into builder state
 * Format: "0 0 0 <day-of-month> <month> <day-of-week>"
 */
export function parseCronToState(cron: string): CronBuilderState {
    const defaultState: CronBuilderState = {
        months: ['*'],
        dayOfMonthMode: 'every',
        daysOfMonth: ['*'],
        daysOfWeek: ['?']
    };

    if (!cron || cron.trim() === '') return defaultState;

    const parts = cron.trim().split(/\s+/);
    if (parts.length !== 6) return defaultState;

    const [, , , dayOfMonth, month, dayOfWeek] = parts;

    // Parse months
    const months = month === '*' ? ['*'] : expandCronField(month, 1, 12).map(String);

    // Parse day of month mode
    let dayOfMonthMode: DayOfMonthMode = 'every';
    let daysOfMonth: string[] = ['*'];

    if (dayOfMonth === 'L') {
        dayOfMonthMode = 'last';
        daysOfMonth = ['L'];
    } else if (dayOfMonth !== '*' && dayOfMonth !== '?') {
        dayOfMonthMode = 'specific';
        daysOfMonth = expandCronField(dayOfMonth, 1, 31).map(String);
    }

    // Parse day of week
    const daysOfWeek = (dayOfWeek === '*' || dayOfWeek === '?')
        ? [dayOfWeek]
        : expandCronField(dayOfWeek, 0, 7).map(String);

    return {
        months,
        dayOfMonthMode,
        daysOfMonth,
        daysOfWeek
    };
}

/**
 * Convert builder state to full 6-field cron expression
 * Format: "0 0 0 <day-of-month> <month> <day-of-week>"
 */
export function stateToCron(state: CronBuilderState): string {
    // Months
    const monthField = state.months.includes('*')
        ? '*'
        : compressToRangesAndLists(state.months.map(Number));

    // Day of Month
    let dayOfMonthField: string;
    let dayOfWeekField: string;

    if (state.dayOfMonthMode === 'last') {
        dayOfMonthField = 'L';
        dayOfWeekField = '?';
    } else if (state.dayOfMonthMode === 'specific') {
        dayOfMonthField = compressToRangesAndLists(state.daysOfMonth.map(Number));
        dayOfWeekField = '?';
    } else {
        // every mode
        dayOfMonthField = '*';
        if (state.daysOfWeek.includes('*') || state.daysOfWeek.includes('?')) {
            dayOfWeekField = '*';
        } else {
            dayOfWeekField = compressToRangesAndLists(state.daysOfWeek.map(Number));
        }
    }

    return `0 0 0 ${dayOfMonthField} ${monthField} ${dayOfWeekField}`;
}

/**
 * Generate human-readable description of cron expression
 */
export function cronToHumanReadable(state: CronBuilderState): string {
    const parts: string[] = [];

    // Day of week or day of month
    if (state.dayOfMonthMode === 'last') {
        parts.push('Last day of month');
    } else if (state.dayOfMonthMode === 'specific') {
        const days = state.daysOfMonth.map(Number).sort((a, b) => a - b);
        if (days.length === 31) {
            parts.push('Every day');
        } else if (days.length === 1) {
            parts.push(`Day ${days[0]}`);
        } else {
            parts.push(`Days ${compressToRangesAndLists(days)}`);
        }
    } else {
        // every mode - check day of week
        if (state.daysOfWeek.includes('*') || state.daysOfWeek.includes('?')) {
            parts.push('Every day');
        } else {
            const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            const days = state.daysOfWeek.map(Number);

            if (days.length === 7) {
                parts.push('Every day');
            } else if (days.length === 5 && days.every(d => d >= 1 && d <= 5)) {
                parts.push('Weekdays');
            } else if (days.length === 2 && days.includes(0) && days.includes(6)) {
                parts.push('Weekends');
            } else {
                parts.push(days.map(d => dayNames[d]).join(', '));
            }
        }
    }

    // Months
    if (!state.months.includes('*')) {
        const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const months = state.months.map(Number).sort((a, b) => a - b);

        if (months.length === 12) {
            // All months - don't add anything
        } else if (months.length === 1) {
            parts.push(`in ${monthNames[months[0] - 1]}`);
        } else {
            parts.push(`in ${months.map(m => monthNames[m - 1]).join(', ')}`);
        }
    }

    return parts.join(' ') || 'Every day';
}

/**
 * Validate the cron builder state
 */
export function validateCronState(state: CronBuilderState): { isValid: boolean; error?: string } {
    // At least one month must be selected
    if (state.months.length === 0 || (state.months.length === 1 && state.months[0] === '')) {
        return { isValid: false, error: 'At least one month must be selected' };
    }

    // Check day of month/week configuration
    if (state.dayOfMonthMode === 'specific') {
        if (state.daysOfMonth.length === 0) {
            return { isValid: false, error: 'At least one day of month must be selected' };
        }
    } else if (state.dayOfMonthMode === 'every') {
        // In "every" mode with day of week selection
        if (!state.daysOfWeek.includes('*') && !state.daysOfWeek.includes('?') && state.daysOfWeek.length === 0) {
            return { isValid: false, error: 'At least one day of week must be selected' };
        }
    }

    return { isValid: true };
}
