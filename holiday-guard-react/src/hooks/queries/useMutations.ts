import {useMutation, useQueryClient} from '@tanstack/react-query';
import {
    createSchedule,
    updateSchedule,
    saveScheduleVersion,
    type VersionPayload
} from '../../services/backend';
import type {Schedule} from '../../types/schedule';

/**
 * Hook to create a new schedule.
 */
export const useCreateSchedule = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (scheduleData: Omit<Schedule, 'id' | 'createdAt'>) => {
            return await createSchedule(scheduleData);
        },
        onSuccess: () => {
            // Invalidate schedules queries to trigger refetch
            queryClient.invalidateQueries({queryKey: ['schedules']});
            queryClient.invalidateQueries({queryKey: ['stats']});
        },
    });
};

/**
 * Hook to update an existing schedule.
 */
export const useUpdateSchedule = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            scheduleId,
            scheduleData
        }: {
            scheduleId: number;
            scheduleData: Omit<Schedule, 'id' | 'createdAt'>;
        }) => {
            return await updateSchedule(scheduleId, scheduleData);
        },
        onSuccess: () => {
            // Invalidate schedules queries to trigger refetch
            queryClient.invalidateQueries({queryKey: ['schedules']});
        },
    });
};

/**
 * Hook to save schedule deviations (creates a new version).
 */
export const useSaveScheduleDeviations = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({
            scheduleId,
            payload
        }: {
            scheduleId: number;
            payload: VersionPayload;
        }) => {
            return await saveScheduleVersion(scheduleId, payload);
        },
        onSuccess: (_data, variables) => {
            // Invalidate deviation-related queries
            queryClient.invalidateQueries({queryKey: ['scheduleDeviations', variables.scheduleId]});
            queryClient.invalidateQueries({queryKey: ['scheduleCalendar', variables.scheduleId]});
            queryClient.invalidateQueries({queryKey: ['multiScheduleCalendar']});
        },
    });
};
