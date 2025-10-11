import { Event } from 'react-big-calendar';
import { RunStatus } from './runStatus';

/**
 * Represents a single day's status (atomic unit without schedule context).
 */
export interface DayStatusView {
    date: string; // ISO date string
    status: RunStatus;
    reason?: string;
}

/**
 * Calendar view for a single schedule over a month.
 * Schedule metadata appears once with all days nested underneath.
 */
export interface ScheduleMonthView {
    scheduleId: number;
    scheduleName: string;
    yearMonth: string; // Format: "YYYY-MM"
    days: DayStatusView[];
}

/**
 * Normalized calendar view for multiple schedules over a month.
 * Eliminates redundancy by storing schedule metadata once per schedule.
 */
export interface MultiScheduleCalendarView {
    yearMonth: string; // Format: "YYYY-MM"
    schedules: ScheduleMonthView[];
}

// Legacy flat structure - kept for backwards compatibility during migration
/** @deprecated Use ScheduleMonthView and DayStatusView instead */
export interface CalendarDay {
    scheduleId: number;
    scheduleName: string;
    date: string; // ISO date string
    status: RunStatus;
    reason?: string;
}

/** @deprecated Use MultiScheduleCalendarView instead */
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
