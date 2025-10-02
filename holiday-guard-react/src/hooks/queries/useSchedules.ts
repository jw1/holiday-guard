import {useQuery} from '@tanstack/react-query';
import {getAllSchedules} from '../../services/backend';
import type {Schedule, ScheduleResponseDto} from '../../types/schedule';

/**
 * Hook to fetch all schedules.
 * Data is cached for 5 minutes and shared across components.
 */
export const useSchedules = () => {
    return useQuery({
        queryKey: ['schedules'],
        queryFn: async (): Promise<Schedule[]> => {
            const data = await getAllSchedules();
            // Transform response to Schedule format
            return data.map((s: ScheduleResponseDto) => ({
                id: s.id,
                name: s.name,
                description: s.description,
                country: s.country,
                active: s.active,
                createdAt: new Date(s.createdAt).toLocaleDateString(),
                ruleType: s.ruleType,
                ruleConfig: s.ruleConfig
            }));
        },
    });
};

/**
 * Hook to fetch only active schedules.
 */
export const useActiveSchedules = () => {
    return useQuery({
        queryKey: ['schedules', 'active'],
        queryFn: async (): Promise<Schedule[]> => {
            const data = await getAllSchedules();
            return data
                .filter(s => s.active)
                .map((s: ScheduleResponseDto) => ({
                    id: s.id,
                    name: s.name,
                    description: s.description,
                    country: s.country,
                    active: s.active,
                    createdAt: new Date(s.createdAt).toLocaleDateString(),
                    ruleType: s.ruleType,
                    ruleConfig: s.ruleConfig
                }));
        },
    });
};
