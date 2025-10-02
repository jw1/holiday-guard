import {useQuery} from '@tanstack/react-query';
import {getAuditLogs} from '../../services/backend';
import type {AuditLog} from '../../types/audit';

/**
 * Hook to fetch all audit logs.
 */
export const useAuditLogs = () => {
    return useQuery({
        queryKey: ['auditLogs'],
        queryFn: async (): Promise<AuditLog[]> => {
            const data = await getAuditLogs();
            // Transform dates
            return data.map(log => ({
                ...log,
                createdAt: new Date(log.createdAt),
            }));
        },
    });
};
