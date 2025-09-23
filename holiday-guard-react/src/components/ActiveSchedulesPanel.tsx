import { FC, useState, useEffect } from 'react';

// Mock data for now - this will be fetched from the backend
const mockData = [
  {
    id: '123e4567-e89b-12d3-a456-426614174000',
    name: 'US Federal Holidays',
    shouldRun: 'No',
    reason: 'JUNETEENTH',
  },
  {
    id: '123e4567-e89b-12d3-a456-426614174001',
    name: 'UK Bank Holidays',
    shouldRun: 'Yes',
    reason: '',
  },
  {
    id: '123e4567-e89b-12d3-a456-426614174002',
    name: 'Canadian Public Holidays',
    shouldRun: 'No',
    reason: 'VICTORIA_DAY',
  },
  {
    id: '123e4567-e89b-12d3-a456-426614174003',
    name: 'Australian Public Holidays',
    shouldRun: 'Yes',
    reason: 'Override: FORCE_RUN',
  },
];

interface ScheduleStatus {
  id: string;
  name: string;
  shouldRun: string;
  reason: string;
}

const ActiveSchedulesPanel: FC = () => {
  const [scheduleStatus, setScheduleStatus] = useState<ScheduleStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // This will be replaced with a fetch call to a new endpoint like /api/v1/schedules/should-run-today
    const fetchScheduleStatus = async () => {
      try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000));
        setScheduleStatus(mockData);
        setLoading(false);
      } catch (err) {
        setError('Failed to fetch schedule status');
        setLoading(false);
      }
    };

    fetchScheduleStatus();
  }, []);

  return (
    <div className="p-6 bg-white rounded-lg shadow-md">
      <h2 className="mb-4 text-xl font-bold">Active Schedules - Run Status Today</h2>
      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">{error}</p>}
      {!loading && !error && (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Schedule ID
                </th>
                <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Schedule Name
                </th>
                <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Should Run?
                </th>
                <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Reason
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {scheduleStatus.map((schedule) => (
                <tr key={schedule.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{schedule.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{schedule.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${schedule.shouldRun === 'Yes' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {schedule.shouldRun}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{schedule.reason}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ActiveSchedulesPanel;
