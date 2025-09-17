import React, { useState, useMemo } from 'react';
import ScheduleModal from './ScheduleModal';

// Define the type for a schedule
export interface Schedule {
  id: number;
  name: string;
  description: string;
  country: string;
  status: 'Active' | 'Inactive';
  createdDate: string;
}

type SortableKey = keyof Schedule;

const sampleSchedules: Schedule[] = [
  {
    id: 1,
    name: 'US Federal Holidays',
    description: 'Standard US federal holidays',
    country: 'US',
    status: 'Active',
    createdDate: '2024-01-15',
  },
  {
    id: 2,
    name: 'UK Bank Holidays',
    description: 'Official bank holidays in the United Kingdom',
    country: 'UK',
    status: 'Active',
    createdDate: '2024-02-01',
  },
  {
    id: 3,
    name: 'Canadian Public Holidays',
    description: 'Statutory holidays for Canada',
    country: 'CA',
    status: 'Inactive',
    createdDate: '2024-03-10',
  },
  {
    id: 4,
    name: 'Australian Public Holidays',
    description: 'Public holidays across Australia',
    country: 'AU',
    status: 'Active',
    createdDate: '2024-04-20',
  },
];

const Schedules = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
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

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingSchedule(null);
  };

  const handleSaveSchedule = (scheduleData: Omit<Schedule, 'id' | 'createdDate'>) => {
    console.log('Saving schedule:', scheduleData);
    handleCloseModal();
  };

  const handleDeleteSchedule = (scheduleId: number) => {
    if (window.confirm('Are you sure you want to delete this schedule?')) {
      console.log('Deleting schedule with id:', scheduleId);
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
    const filtered = sampleSchedules.filter(schedule =>
      schedule.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return [...filtered].sort((a, b) => {
      const aValue = a[sortColumn];
      const bValue = b[sortColumn];

      if (aValue < bValue) return sortDirection === 'ascending' ? -1 : 1;
      if (aValue > bValue) return sortDirection === 'ascending' ? 1 : -1;
      return 0;
    });
  }, [searchTerm, sortColumn, sortDirection]);

  const SortableHeader = ({ columnKey, title }: { columnKey: SortableKey, title: string }) => (
    <th
      scope="col"
      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
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
        <button onClick={handleNewSchedule} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          New Schedule
        </button>
      </div>

      <div className="bg-white shadow-md rounded-lg overflow-hidden">
        <div className="p-4">
          <div className="relative">
            <input
              type="text"
              placeholder="Search by name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline pr-10"
            />
            {searchTerm && (
              <button onClick={() => setSearchTerm('')} className="absolute inset-y-0 right-0 flex items-center pr-3">
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            )}
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <SortableHeader columnKey="name" title="Name" />
                <SortableHeader columnKey="description" title="Description" />
                <SortableHeader columnKey="country" title="Country" />
                <SortableHeader columnKey="status" title="Status" />
                <SortableHeader columnKey="createdDate" title="Created Date" />
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {sortedSchedules.map((schedule) => (
                <tr key={schedule.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{schedule.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{schedule.description}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{schedule.country}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${schedule.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                      {schedule.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{schedule.createdDate}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button onClick={() => handleEditSchedule(schedule)} className="text-indigo-600 hover:text-indigo-900 mr-4 text-xs">Edit</button>
                    <button onClick={() => handleDeleteSchedule(schedule.id)} className="text-red-600 hover:text-red-900 text-xs">Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {isModalOpen && (
        <ScheduleModal
          schedule={editingSchedule}
          onClose={handleCloseModal}
          onSave={handleSaveSchedule}
        />
      )}
    </main>
  );
};

export default Schedules;