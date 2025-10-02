/**
 * Parse an ISO date string (YYYY-MM-DD) to a Date object in local timezone.
 *
 * This avoids the timezone conversion issue with `new Date("2025-09-21")` which
 * interprets the string as UTC midnight and converts to local timezone, causing
 * off-by-one errors for users in timezones west of UTC.
 *
 * @param isoDateString - ISO date string in format "YYYY-MM-DD"
 * @returns Date object set to midnight in local timezone
 */
export function parseISODateString(isoDateString: string): Date {
    const [year, month, day] = isoDateString.split('-').map(Number);
    // Month is 0-indexed in JavaScript Date constructor
    return new Date(year, month - 1, day);
}

/**
 * Format a Date object to ISO date string (YYYY-MM-DD).
 *
 * @param date - Date object
 * @returns ISO date string in format "YYYY-MM-DD"
 */
export function formatToISODateString(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}
