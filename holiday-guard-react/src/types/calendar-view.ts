import { Event } from 'react-big-calendar';

/**
 * Represents a single calendar day for a specific schedule.
 */
export interface CalendarDay {
    scheduleId: number;
    scheduleName: string;
    date: string; // ISO date string
    status: 'run' | 'no-run' | 'FORCE_RUN' | 'SKIP';
    reason?: string;
}

/**
 * Container for calendar data across multiple schedules.
 */
export interface MultiScheduleCalendar {
    yearMonth: string; // Format: "YYYY-MM"
    days: CalendarDay[];
}

/**
 * React-big-calendar event with our custom data.
 */
export interface CalendarEvent extends Event {
    scheduleId: number;
    scheduleName: string;
    status: 'run' | 'no-run' | 'FORCE_RUN' | 'SKIP';
    reason?: string;
}

/**
 * Filter state for controlling which schedules and statuses to display.
 */
export interface CalendarFilters {
    selectedScheduleIds: number[];
    showRun: boolean;
    showNoRun: boolean;
    showForceRun: boolean;
    showSkip: boolean;
}
