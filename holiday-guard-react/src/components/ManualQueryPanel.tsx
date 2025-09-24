import { FC, useState, useEffect } from 'react';

// From ScheduleController.java -> getAllSchedules()
interface ScheduleInfo {
  id: string;
  name: string;
}

// From ShouldRunController.java -> shouldRunToday()
interface ShouldRunResponse {
  scheduleId: string;
  queryDate: string;
  shouldRun: boolean;
  reason: string;
  overrideApplied: boolean;
  versionId: string;
}

const ManualQueryPanel: FC = () => {
  const [schedules, setSchedules] = useState<ScheduleInfo[]>([]);
  const [selectedScheduleId, setSelectedScheduleId] = useState<string>('');
  const [clientIdentifier, setClientIdentifier] = useState('admin-ui');
  const [queryResult, setQueryResult] = useState<ShouldRunResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Fetch all schedules to populate the dropdown
    const fetchSchedules = async () => {
      try {
        const response = await fetch('/api/v1/schedules');
        if (!response.ok) throw new Error('Failed to fetch schedules');
        const data: ScheduleInfo[] = await response.json();
        setSchedules(data);
        if (data.length > 0) {
          setSelectedScheduleId(data[0].id);
        }
      } catch (err) {
        setError('Could not load schedule list.');
      }
    };
    fetchSchedules();
  }, []);

  const handleQuerySubmit = async () => {
    if (!selectedScheduleId) {
      alert('Please select a schedule.');
      return;
    }
    setLoading(true);
    setError(null);
    setQueryResult(null);

    try {
      const url = `/api/v1/schedules/${selectedScheduleId}/should-run?client=${encodeURIComponent(clientIdentifier)}`;
      const response = await fetch(url);
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Query failed');
      }
      const data: ShouldRunResponse = await response.json();
      setQueryResult(data);
    } catch (err: any) {
      setError(err.message || 'An unknown error occurred.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 bg-white rounded-lg shadow mb-8">
      <h3 className="text-xl font-bold mb-4">Manual 'Should Run' Query</h3>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
        {/* Schedule Dropdown */}
        <div className="col-span-1">
          <label htmlFor="schedule-select" className="block text-sm font-medium text-gray-700">Schedule</label>
          <select
            id="schedule-select"
            value={selectedScheduleId}
            onChange={(e) => setSelectedScheduleId(e.target.value)}
            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
          >
            {schedules.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>

        {/* Client Identifier Input */}
        <div className="col-span-1">
          <label htmlFor="client-id" className="block text-sm font-medium text-gray-700">Client Identifier</label>
          <input
            type="text"
            id="client-id"
            value={clientIdentifier}
            onChange={(e) => setClientIdentifier(e.target.value)}
            className="mt-1 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
          />
        </div>

        {/* Submit Button */}
        <div className="col-span-1">
          <button
            onClick={handleQuerySubmit}
            disabled={loading}
            className="w-full inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400"
          >
            {loading ? 'Querying...' : 'Run Query'}
          </button>
        </div>
      </div>

      {/* Result Display */}
      {error && <div className="mt-4 p-4 bg-red-50 text-red-700 rounded-md">Error: {error}</div>}
      {queryResult && (
        <div className="mt-4 p-4 bg-gray-50 rounded-md">
          <h4 className="text-lg font-semibold mb-2">Query Result</h4>
          <div className="flex items-center">
            <span className="font-medium mr-2">Should Run?</span>
            <span className={`px-3 py-1 inline-flex text-sm leading-5 font-semibold rounded-full ${queryResult.shouldRun ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
              {queryResult.shouldRun ? 'Yes' : 'No'}
            </span>
          </div>
          <div className="mt-2">
            <span className="font-medium">Reason:</span>
            <span className="ml-2 text-gray-700">{queryResult.reason}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManualQueryPanel;
