import {useQuery} from '@tanstack/react-query';
import {
    getMultiScheduleCalendar,
    getScheduleCalendar,
    getScheduleDeviations,
    type CalendarResponse,
    type Deviation
} from '../../services/backend';
import type {MultiScheduleCalendarView} from '../../types/calendar-view';

/**
 * Hook to fetch calendar data for multiple schedules.
 * Returns the NEW normalized structure (MultiScheduleCalendarView).
 */
export const useMultiScheduleCalendar = (
    scheduleIds: number[],
    yearMonth: string,
    includeDeviations: boolean = true
) => {
    return useQuery({
        queryKey: ['multiScheduleCalendar', scheduleIds, yearMonth, includeDeviations],
        queryFn: async (): Promise<MultiScheduleCalendarView> => {
            console.log('[useMultiScheduleCalendar] Fetching calendar for schedules:', scheduleIds, 'yearMonth:', yearMonth);
            const result = await getMultiScheduleCalendar(scheduleIds, yearMonth, includeDeviations);
            console.log('[useMultiScheduleCalendar] Received data:', result);
            return result;
        },
        enabled: scheduleIds.length > 0, // Only fetch if we have schedule IDs
    });
};

/**
 * Hook to fetch base calendar for a single schedule.
 */
export const useScheduleCalendar = (scheduleId: number | null, yearMonth: string) => {
    return useQuery({
        queryKey: ['scheduleCalendar', scheduleId, yearMonth],
        queryFn: async (): Promise<CalendarResponse> => {
            if (!scheduleId) throw new Error('Schedule ID is required');
            return await getScheduleCalendar(scheduleId, yearMonth);
        },
        enabled: !!scheduleId, // Only fetch if we have a schedule ID
    });
};

/**
 * Hook to fetch deviations for a schedule.
 */
export const useScheduleDeviations = (scheduleId: number | null) => {
    return useQuery({
        queryKey: ['scheduleDeviations', scheduleId],
        queryFn: async (): Promise<Deviation[]> => {
            if (!scheduleId) throw new Error('Schedule ID is required');
            return await getScheduleDeviations(scheduleId);
        },
        enabled: !!scheduleId, // Only fetch if we have a schedule ID
    });
};
