import { useState, useEffect, useMemo } from 'react';
import ScheduleModal from './ScheduleModal';
import OverridesModal from './OverridesModal';

// Define the type for a schedule on the frontend
export interface Schedule {
  id: string; // UUID is a string
  name: string;
  description: string;
  country: string;
  active: boolean;
  createdDate: string; // Formatted date string
  ruleType: string;
  ruleConfig: string;
}

// Define the type for the raw data coming from the backend API
interface ScheduleResponseDto {
  id: string;
  name: string;
  description: string;
  country: string;
  active: boolean;
  createdAt: string; // ISO date string
  updatedAt: string;
  ruleType: string;
  ruleConfig: string;
}

type SortableKey = keyof Schedule;

const Schedules = () => {
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isOverridesModalOpen, setIsOverridesModalOpen] = useState(false);
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortColumn, setSortColumn] = useState<SortableKey>('name');
  const [sortDirection, setSortDirection] = useState('ascending');

  useEffect(() => {
    fetch('/api/v1/schedules')
      .then(res => res.json())
      .then((data: ScheduleResponseDto[]) => {
        const formattedData: Schedule[] = data.map(s => ({
          id: s.id,
          name: s.name,
          description: s.description,
          country: s.country,
          active: s.active,
          createdDate: new Date(s.createdAt).toLocaleDateString(),
          ruleType: s.ruleType,
          ruleConfig: s.ruleConfig,
        }));
        setSchedules(formattedData);
      })
      .catch(error => console.error('Error fetching schedules:', error));
  }, []);

  const handleNewSchedule = () => {
    setEditingSchedule(null);
    setIsModalOpen(true);
  };

  const handleEditSchedule = (schedule: Schedule) => {
    setEditingSchedule(schedule);
    setIsModalOpen(true);
  };

  const handleOverridesClick = (schedule: Schedule) => {
    setEditingSchedule(schedule);
    setIsOverridesModalOpen(true);
  };

  const handleCloseOverridesModal = () => {
    setIsOverridesModalOpen(false);
    setEditingSchedule(null);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingSchedule(null);
  };

  const handleSaveSchedule = (scheduleData: Omit<Schedule, 'id' | 'createdDate'>) => {
    const isNew = !editingSchedule;
    const url = isNew ? '/api/v1/schedules' : `/api/v1/schedules/${editingSchedule.id}`;
    const method = isNew ? 'POST' : 'PUT';

    fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(scheduleData),
    })
      .then(res => {
        if (!res.ok) {
          throw new Error('Save failed');
        }
        return res.json();
      })
      .then((savedSchedule: ScheduleResponseDto) => {
        const formatted: Schedule = {
          id: savedSchedule.id,
          name: savedSchedule.name,
          description: savedSchedule.description,
          country: savedSchedule.country,
          active: savedSchedule.active,
          createdDate: new Date(savedSchedule.createdAt).toLocaleDateString(),
          ruleType: savedSchedule.ruleType,
          ruleConfig: savedSchedule.ruleConfig,
        };

        if (isNew) {
          setSchedules([...schedules, formatted]);
        } else {
          setSchedules(schedules.map(s => (s.id === formatted.id ? formatted : s)));
        }
        handleCloseModal();
      })
      .catch(error => {
        console.error('Error saving schedule:', error);
        window.alert('Failed to save schedule. Please try again.');
      });
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

  const SortableHeader = ({ columnKey, title }: { columnKey: SortableKey, title: string }) => (
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
        <button onClick={handleNewSchedule} className="px-4 py-2 font-bold text-white bg-blue-500 rounded hover:bg-blue-700">
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
              <button onClick={() => setSearchTerm('')} className="absolute inset-y-0 right-0 flex items-center pr-3">
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                <SortableHeader columnKey="id" title="ID" />
                <SortableHeader columnKey="name" title="Name" />
                <SortableHeader columnKey="description" title="Description" />
                <SortableHeader columnKey="active" title="Status" />
                <SortableHeader columnKey="createdDate" title="Created Date" />
                <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {sortedSchedules.map((schedule) => (
                <tr key={schedule.id}>
                  <td className="px-6 py-4 text-xs font-mono text-gray-500 whitespace-nowrap">{schedule.id}</td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900 whitespace-nowrap">{schedule.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{schedule.description}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${schedule.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                      {schedule.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">{schedule.createdDate}</td>
                  <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                    <button onClick={() => handleEditSchedule(schedule)} className="mr-4 text-xs text-indigo-600 hover:text-indigo-900">Edit</button>
                    <button onClick={() => handleOverridesClick(schedule)} className="mr-4 text-xs text-indigo-600 hover:text-indigo-900">Overrides</button>
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
      {isOverridesModalOpen && (
        <OverridesModal
          schedule={editingSchedule}
          onClose={handleCloseOverridesModal}
          onSave={() => {}}
        />
      )}
    </main>
  );
};

export default Schedules;