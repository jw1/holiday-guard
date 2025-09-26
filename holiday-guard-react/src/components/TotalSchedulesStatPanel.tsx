import { FC, useState, useEffect } from 'react';

interface StatCount {
  count: number;
}

const TotalSchedulesStatPanel: FC = () => {

  const [count, setCount] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTotalCount = async () => {
      try {
        const response = await fetch('/api/v1/dashboard/stats/total-schedules');
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data: StatCount = await response.json();
        setCount(data.count);
      } catch (err) {
        setError('Failed to fetch');
      }
    };

    fetchTotalCount();
  }, []);

  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-gray-500 text-sm">Total Schedules</h3>
      <p className="text-2xl font-bold">
        {count !== null ? count : 'Loading...'}
        {error && <span className="text-red-500 text-sm"> {error}</span>}
      </p>
    </div>
  );
};

export default TotalSchedulesStatPanel;
