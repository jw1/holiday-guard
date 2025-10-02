import {useState, useMemo} from 'react';
import ScheduleModal from '@/components/ScheduleModal';
import DeviationsModal from '@/components/DeviationsModal';
import type {Schedule} from '@/types/schedule';
import type {VersionPayload} from '../services/backend';
import {useSchedules, useCreateSchedule, useUpdateSchedule, useSaveScheduleDeviations} from '../hooks/queries';

type SortableKey = keyof Schedule;

const Schedules = () => {

    const {data: schedules = [], isLoading, error} = useSchedules();
    const createScheduleMutation = useCreateSchedule();
    const updateScheduleMutation = useUpdateSchedule();
    const saveDeviationsMutation = useSaveScheduleDeviations();

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDeviationsModalOpen, setIsDeviationsModalOpen] = useState(false);
    const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortColumn, setSortColumn] = useState<SortableKey>('name');
    const [sortDirection, setSortDirection] = useState('ascending');

    const handleNewSchedule = () => {
        setEditingSchedule(null);
        setIsModalOpen(true);
    };

    const handleEditSchedule = (schedule: Schedule) => {
        setEditingSchedule(schedule);
        setIsModalOpen(true);
    };

    const handleDeviationsClick = (schedule: Schedule) => {
        setEditingSchedule(schedule);
        setIsDeviationsModalOpen(true);
    };

    const handleCloseDeviationsModal = () => {
        setIsDeviationsModalOpen(false);
        setEditingSchedule(null);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingSchedule(null);
    };

    const handleSaveSchedule = async (scheduleData: Omit<Schedule, 'id' | 'createdAt'>) => {
        const isNew = !editingSchedule;

        try {
            if (isNew) {
                await createScheduleMutation.mutateAsync(scheduleData);
            } else {
                await updateScheduleMutation.mutateAsync({
                    scheduleId: editingSchedule!.id,
                    scheduleData,
                });
            }
            handleCloseModal();
        } catch (error) {
            console.error('Error saving schedule:', error);
            window.alert('Failed to save schedule. Please try again.');
        }
    };

    const handleSaveDeviations = async (payload: VersionPayload) => {
        if (!editingSchedule) return;

        try {
            await saveDeviationsMutation.mutateAsync({
                scheduleId: editingSchedule.id,
                payload,
            });
            handleCloseDeviationsModal();
        } catch (error) {
            console.error('Error saving deviations:', error);
            window.alert('Failed to save deviations. Please try again.');
        }
    };

    const handleSort = (columnKey: SortableKey) => {
        if (sortColumn === columnKey) {
            setSortDirection(sortDirection === 'ascending' ? 'descending' : 'ascending');
        } else {
            setSortColumn(columnKey);
            setSortDirection('ascending');
        }
    };

    const sortedSchedules = useMemo(() => {
        const filtered = schedules.filter(schedule =>
            schedule.name.toLowerCase().includes(searchTerm.toLowerCase())
        );

        return [...filtered].sort((a, b) => {
            const aValue = a[sortColumn];
            const bValue = b[sortColumn];

            if (aValue < bValue) return sortDirection === 'ascending' ? -1 : 1;
            if (aValue > bValue) return sortDirection === 'ascending' ? 1 : -1;
            return 0;
        });
    }, [schedules, searchTerm, sortColumn, sortDirection]);

    const SortableHeader = ({columnKey, title}: { columnKey: SortableKey, title: string }) => (
        <th
            scope="col"
            className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase cursor-pointer"
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
                <h1 className="text-3xl font-bold">Schedules</h1>
                <button onClick={handleNewSchedule}
                        className="px-4 py-2 font-bold text-white bg-blue-500 rounded hover:bg-blue-700">
                    New Schedule
                </button>
            </div>

            <div className="overflow-hidden bg-white rounded-lg shadow-md">
                <div className="p-4">
                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Search by name..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full px-3 py-2 pr-10 leading-tight text-gray-700 border rounded shadow appearance-none focus:outline-none focus:shadow-outline"
                        />
                        {searchTerm && (
                            <button onClick={() => setSearchTerm('')}
                                    className="absolute inset-y-0 right-0 flex items-center pr-3">
                                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor"
                                     viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                                          d="M6 18L18 6M6 6l12 12"></path>
                                </svg>
                            </button>
                        )}
                    </div>
                </div>
                {isLoading && <div className="p-8 text-center text-gray-500">Loading schedules...</div>}
                {error && <div className="p-8 text-center text-red-500">Failed to fetch schedules</div>}
                {!isLoading && !error && (
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <SortableHeader columnKey="id" title="ID"/>
                            <SortableHeader columnKey="name" title="Name"/>
                            <SortableHeader columnKey="description" title="Description"/>
                            <SortableHeader columnKey="active" title="Status"/>
                            <SortableHeader columnKey="createdAt" title="Created At"/>
                            <th scope="col"
                                className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Actions
                            </th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {sortedSchedules.map((schedule) => (
                            <tr key={schedule.id}>
                                <td className="px-6 py-4 text-xs font-mono text-gray-500 whitespace-nowrap">{schedule.id}</td>
                                <td className="px-6 py-4 text-sm font-medium text-gray-900 whitespace-nowrap">{schedule.name}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{schedule.description}</td>
                                <td className="px-6 py-4 whitespace-nowrap">
                    <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${schedule.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                      {schedule.active ? 'Active' : 'Inactive'}
                    </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">{schedule.createdAt}</td>
                                <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                                    <button onClick={() => handleEditSchedule(schedule)}
                                            className="mr-4 text-xs text-indigo-600 hover:text-indigo-900">Edit
                                    </button>
                                    <button onClick={() => handleDeviationsClick(schedule)}
                                            className="mr-4 text-xs text-indigo-600 hover:text-indigo-900">Deviations
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
                )}
            </div>

            {isModalOpen && (
                <ScheduleModal
                    schedule={editingSchedule}
                    onClose={handleCloseModal}
                    onSave={handleSaveSchedule}
                />
            )}
            {isDeviationsModalOpen && (
                <DeviationsModal
                    schedule={editingSchedule}
                    onClose={handleCloseDeviationsModal}
                    onSave={handleSaveDeviations}
                />
            )}
        </main>
    );
};

export default Schedules;