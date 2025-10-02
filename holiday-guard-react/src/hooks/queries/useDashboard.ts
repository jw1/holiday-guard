import {useQuery} from '@tanstack/react-query';
import {
    getScheduleStatus,
    getTotalSchedulesCount,
    getActiveSchedulesCount,
    getHealthStatus,
    type DailyScheduleStatus,
    type HealthStatus
} from '../../services/backend';

/**
 * Hook to fetch daily schedule status for all active schedules.
 */
export const useScheduleStatus = () => {
    return useQuery({
        queryKey: ['scheduleStatus'],
        queryFn: async (): Promise<DailyScheduleStatus[]> => {
            return await getScheduleStatus();
        },
        staleTime: 1000 * 60 * 2, // 2 minutes - refresh more frequently
    });
};

/**
 * Hook to fetch total schedules count.
 */
export const useTotalSchedulesCount = () => {
    return useQuery({
        queryKey: ['stats', 'totalSchedules'],
        queryFn: async (): Promise<number> => {
            return await getTotalSchedulesCount();
        },
    });
};

/**
 * Hook to fetch active schedules count.
 */
export const useActiveSchedulesCount = () => {
    return useQuery({
        queryKey: ['stats', 'activeSchedules'],
        queryFn: async (): Promise<number> => {
            return await getActiveSchedulesCount();
        },
    });
};

/**
 * Hook to fetch server health status.
 */
export const useHealthStatus = () => {
    return useQuery({
        queryKey: ['health'],
        queryFn: async (): Promise<HealthStatus> => {
            return await getHealthStatus();
        },
        staleTime: 1000 * 30, // 30 seconds
        retry: 2,
    });
};
