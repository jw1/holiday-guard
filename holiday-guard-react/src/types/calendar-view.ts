import { Event } from 'react-big-calendar';
import { RunStatus } from './runStatus';

/**
 * Represents a single calendar day for a specific schedule.
 */
export interface CalendarDay {
    scheduleId: number;
    scheduleName: string;
    date: string; // ISO date string
    status: RunStatus;
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
    status: RunStatus;
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
