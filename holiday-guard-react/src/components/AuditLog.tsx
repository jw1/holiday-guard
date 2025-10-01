import {useState, useEffect, useMemo} from 'react';
import {format} from 'date-fns';
import type {AuditLog, AuditLogDto} from '@/types/audit';
import api from '../services/api';

type SortableKey = keyof AuditLog;

const AuditLogPage = () => {

    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortColumn, setSortColumn] = useState<SortableKey>('createdAt');
    const [sortDirection, setSortDirection] = useState('descending');

    useEffect(() => {
        const fetchAuditLogs = async () => {
            try {
                const response = await api.get<AuditLogDto[]>('/audit-logs');

                const formattedData: AuditLog[] = response.data.map(log => ({
                    ...log,
                    createdAt: new Date(log.createdAt),
                }));

                setLogs(formattedData);
            } catch (err) {
                console.error('Error fetching audit logs:', err);
            }
        };

        void fetchAuditLogs();
    }, []);

    const handleSort = (columnKey: SortableKey) => {
        if (sortColumn === columnKey) {
            setSortDirection(sortDirection === 'ascending' ? 'descending' : 'ascending');
        } else {
            setSortColumn(columnKey);
            setSortDirection('ascending');
        }
    };

    const sortedLogs = useMemo(() => {

        const filtered = logs.filter(log =>
            log.scheduleName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.clientIdentifier.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.reason.toLowerCase().includes(searchTerm.toLowerCase())
        );

        return [...filtered].sort((a, b) => {
            const aValue = a[sortColumn];
            const bValue = b[sortColumn];

            if (aValue < bValue) return sortDirection === 'ascending' ? -1 : 1;
            if (aValue > bValue) return sortDirection === 'ascending' ? 1 : -1;
            return 0;
        });
    }, [logs, searchTerm, sortColumn, sortDirection]);

    const SortableHeader = ({columnKey, title}: { columnKey: SortableKey, title: string }) => (
        <th
            scope="col"
            className="px-4 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase cursor-pointer"
            onClick={() => handleSort(columnKey)}
        >
            {title}
            {sortColumn === columnKey && (
                <span>{sortDirection === 'ascending' ? ' ▲' : ' ▼'}</span>
            )}
        </th>
    );

    return (
        <main className="flex-1 p-6 sm:p-10">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-3xl font-bold">Audit Log</h1>
            </div>

            <div className="overflow-hidden bg-white rounded-lg shadow-md">
                <div className="p-4">
                    <input
                        type="text"
                        placeholder="Search by name, client, or reason..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full px-3 py-2 pr-10 leading-tight text-gray-700 border rounded shadow appearance-none focus:outline-none focus:shadow-outline"
                    />
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <SortableHeader columnKey="createdAt" title="Timestamp"/>
                            <SortableHeader columnKey="scheduleName" title="Schedule Name"/>
                            <SortableHeader columnKey="shouldRunResult" title="Result"/>
                            <SortableHeader columnKey="reason" title="Reason"/>
                            <SortableHeader columnKey="clientIdentifier" title="Client"/>
                            <SortableHeader columnKey="scheduleId" title="Schedule ID"/>
                            <SortableHeader columnKey="versionId" title="Version ID"/>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {sortedLogs.map((log) => (
                            <tr key={log.logId}>
                                <td className="px-4 py-3 text-sm text-gray-500 whitespace-nowrap">{format(log.createdAt, 'yyyy-MM-dd HH:mm:ss')}</td>
                                <td className="px-4 py-3 text-sm font-medium text-gray-900 whitespace-nowrap">{log.scheduleName}</td>
                                <td className="px-4 py-3 text-sm whitespace-nowrap">
                    <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${log.shouldRunResult ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {log.shouldRunResult ? 'RUN' : 'NO RUN'}
                    </span>
                                </td>
                                <td className="px-4 py-3 text-sm text-gray-500 truncate max-w-xs"
                                    title={log.reason}>{log.reason}</td>
                                <td className="px-4 py-3 text-sm text-gray-500 whitespace-nowrap">{log.clientIdentifier}</td>
                                <td className="px-4 py-3 text-sm font-mono text-gray-500 whitespace-nowrap">{log.scheduleId}</td>
                                <td className="px-4 py-3 text-sm font-mono text-gray-500 whitespace-nowrap">{log.versionId}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    );
};

export default AuditLogPage;
