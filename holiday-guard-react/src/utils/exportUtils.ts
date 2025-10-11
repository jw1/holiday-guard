import moment from 'moment';
import { CalendarEvent } from '../types/calendar-view';
import { RunStatus } from '../types/runStatus';

/**
 * Export calendar events to CSV format
 */
export const exportToCSV = (events: CalendarEvent[], filename: string = 'calendar-export.csv') => {
    // CSV header
    const headers = ['Date', 'Schedule', 'Status', 'Reason'];

    // Convert events to CSV rows
    const rows = events.map(event => {
        const date = moment(event.start).format('YYYY-MM-DD');
        const schedule = event.resource?.scheduleName || '';
        const status = formatStatus(event.resource?.status || RunStatus.RUN);
        const reason = event.resource?.reason || '';

        // Escape CSV values (handle commas, quotes, newlines)
        return [
            date,
            escapeCSV(schedule),
            status,
            escapeCSV(reason)
        ].join(',');
    });

    // Combine header and rows
    const csvContent = [headers.join(','), ...rows].join('\n');

    // Create blob and download
    downloadFile(csvContent, filename, 'text/csv;charset=utf-8;');
};

/**
 * Export calendar events to ICS (iCalendar) format
 */
export const exportToICS = (events: CalendarEvent[], filename: string = 'calendar-export.ics') => {
    // ICS header
    const icsLines = [
        'BEGIN:VCALENDAR',
        'VERSION:2.0',
        'PRODID:-//Holiday Guard//Schedule Viewer//EN',
        'CALSCALE:GREGORIAN',
        'METHOD:PUBLISH',
    ];

    // Add each event
    events.forEach(event => {
        const date = moment(event.start).format('YYYYMMDD');
        const scheduleName = event.resource?.scheduleName || 'Unknown';
        const status = formatStatus(event.resource?.status || RunStatus.RUN);
        const reason = event.resource?.reason || '';

        // Create event summary and description
        const summary = `${scheduleName} - ${status}`;
        const description = reason
            ? `Status: ${status}\\nReason: ${reason}`
            : `Status: ${status}`;

        icsLines.push(
            'BEGIN:VEVENT',
            `DTSTART;VALUE=DATE:${date}`,
            `DTEND;VALUE=DATE:${date}`,
            `SUMMARY:${escapeICS(summary)}`,
            `DESCRIPTION:${escapeICS(description)}`,
            `UID:${event.resource?.scheduleId}-${date}@holidayguard.com`,
            `DTSTAMP:${moment().format('YYYYMMDDTHHmmss')}Z`,
            'BEGIN:VALARM',
            'ACTION:NONE',
            'TRIGGER;VALUE=DATE-TIME:19760401T005545Z',
            'END:VALARM',
            'END:VEVENT'
        );
    });

    // ICS footer
    icsLines.push('END:VCALENDAR');

    const icsContent = icsLines.join('\r\n');

    // Create blob and download
    downloadFile(icsContent, filename, 'text/calendar;charset=utf-8;');
};

/**
 * Format status for display
 */
const formatStatus = (status: RunStatus): string => {
    switch (status) {
        case RunStatus.FORCE_RUN: return 'Force Run';
        case RunStatus.FORCE_SKIP: return 'Force Skip';
        case RunStatus.RUN: return 'Run';
        case RunStatus.SKIP: return 'Skip';
        default: return status;
    }
};

/**
 * Escape CSV values
 */
const escapeCSV = (value: string): string => {
    if (!value) return '';

    // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
    if (value.includes(',') || value.includes('"') || value.includes('\n')) {
        return `"${value.replace(/"/g, '""')}"`;
    }

    return value;
};

/**
 * Escape ICS values (simple escaping for special characters)
 */
const escapeICS = (value: string): string => {
    return value
        .replace(/\\/g, '\\\\')
        .replace(/;/g, '\\;')
        .replace(/,/g, '\\,')
        .replace(/\n/g, '\\n');
};

/**
 * Download a file
 */
const downloadFile = (content: string, filename: string, mimeType: string) => {
    const blob = new Blob([content], { type: mimeType });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
};
