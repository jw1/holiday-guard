/**
 * Backend API service layer for Holiday Guard.
 */

import api from './api';
import {ScheduleResponseDto, Schedule} from '../types/schedule';
import {AuditLogDto} from '../types/audit';

// ============================================================================
// Type Definitions
// ============================================================================

export interface DailyScheduleStatus {
    scheduleId: number;
    scheduleName: string;
    shouldRun: boolean;
    reason: string;
}

export interface StatCount {
    count: number;
}

export interface HealthStatus {
    status: string;
}

export interface ScheduleInfo {
    id: number;
    name: string;
}

export interface ShouldRunResponse {
    scheduleId: number;
    queryDate: string;
    shouldRun: boolean;
    reason: string;
    deviationApplied: boolean;
    versionId: number;
}

export interface Deviation {
    date: string;
    type: 'FORCE_RUN' | 'SKIP';
    reason: string;
}

export interface CalendarResponse {
    yearMonth: string;
    days: Record<string, 'run' | 'no-run'>;
}

export interface VersionPayload {
    effectiveFrom?: string;
    rule: {
        ruleType: string;
        ruleConfig: string;
        effectiveFrom: string;
        active: boolean;
    };
    deviations?: Array<{
        deviationDate: string;
        action: string;
        reason: string;
    }>;
}

// ============================================================================
// Dashboard Services
// ============================================================================

/**
 * Fetches the daily run status for all active schedules.
 */
export const getScheduleStatus = async (): Promise<DailyScheduleStatus[]> => {
    const response = await api.get<DailyScheduleStatus[]>('/dashboard/schedule-status');
    return response.data;
};

/**
 * Fetches the total count of all schedules.
 */
export const getTotalSchedulesCount = async (): Promise<number> => {
    const response = await api.get<StatCount>('/dashboard/stats/total-schedules');
    return response.data.count;
};

/**
 * Fetches the count of active schedules.
 */
export const getActiveSchedulesCount = async (): Promise<number> => {
    const response = await api.get<StatCount>('/dashboard/stats/active-schedules');
    return response.data.count;
};

// ============================================================================
// Schedule Services
// ============================================================================

/**
 * Fetches all schedules.
 */
export const getAllSchedules = async (): Promise<ScheduleResponseDto[]> => {
    const response = await api.get<ScheduleResponseDto[]>('/schedules');
    return response.data;
};

/**
 * Fetches a list of basic schedule info (id and name only).
 */
export const getScheduleList = async (): Promise<ScheduleInfo[]> => {
    const response = await api.get<ScheduleInfo[]>('/schedules');
    return response.data;
};

/**
 * Creates a new schedule.
 */
export const createSchedule = async (
    scheduleData: Omit<Schedule, 'id' | 'createdAt'>
): Promise<ScheduleResponseDto> => {
    const response = await api.post<ScheduleResponseDto>('/schedules', scheduleData);
    return response.data;
};

/**
 * Updates an existing schedule.
 */
export const updateSchedule = async (
    scheduleId: number,
    scheduleData: Omit<Schedule, 'id' | 'createdAt'>
): Promise<ScheduleResponseDto> => {
    const response = await api.put<ScheduleResponseDto>(`/schedules/${scheduleId}`, scheduleData);
    return response.data;
};

/**
 * Queries whether a schedule should run for a given client.
 */
export const shouldScheduleRun = async (
    scheduleId: number,
    clientIdentifier: string
): Promise<ShouldRunResponse> => {
    const response = await api.get<ShouldRunResponse>(
        `/schedules/${scheduleId}/should-run`,
        {params: {client: clientIdentifier}}
    );
    return response.data;
};

// ============================================================================
// Deviation Services
// ============================================================================

/**
 * Fetches all deviations for a specific schedule.
 */
export const getScheduleDeviations = async (scheduleId: number): Promise<Deviation[]> => {
    const response = await api.get<Deviation[]>(`/schedules/${scheduleId}/deviations`);
    return response.data;
};

/**
 * Fetches the base calendar for a schedule for a given month.
 */
export const getScheduleCalendar = async (
    scheduleId: number,
    yearMonth: string
): Promise<CalendarResponse> => {
    const response = await api.get<CalendarResponse>(
        `/schedules/${scheduleId}/calendar`,
        {params: {yearMonth}}
    );
    return response.data;
};

/**
 * Creates a new version with updated deviations for a schedule.
 */
export const saveScheduleVersion = async (
    scheduleId: number,
    payload: VersionPayload
): Promise<void> => {
    await api.post(`/schedules/${scheduleId}/versions`, payload);
};

// ============================================================================
// Audit Log Services
// ============================================================================

/**
 * Fetches all audit logs.
 */
export const getAuditLogs = async (): Promise<AuditLogDto[]> => {
    const response = await api.get<AuditLogDto[]>('/audit-logs');
    return response.data;
};

// ============================================================================
// Health Check Services
// ============================================================================

/**
 * Fetches the server health status.
 * Note: This uses axios directly since /actuator/health is outside /api/v1.
 */
export const getHealthStatus = async (): Promise<HealthStatus> => {
    const axios = (await import('axios')).default;
    const response = await axios.get<HealthStatus>('/actuator/health');
    return response.data;
};
