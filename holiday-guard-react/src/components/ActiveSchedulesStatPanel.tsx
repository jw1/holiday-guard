import {FC, useState, useEffect} from 'react';

interface StatCount {
    count: number;
}

const ActiveSchedulesStatPanel: FC = () => {

    const [count, setCount] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchActiveCount = async () => {
            try {
                const response = await fetch('/api/v1/dashboard/stats/active-schedules');

                if (!response.ok) {
                    setError('Failed to fetch');
                } else {
                    const data: StatCount = await response.json();
                    setCount(data.count);
                }
            } catch (err) {
                setError('Failed to fetch');
            }
        };

        void fetchActiveCount();
    }, []);

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-gray-500 text-sm">Active Schedules</h3>
            <p className="text-2xl font-bold">
                {count !== null ? count : 'Loading...'}
                {error && <span className="text-red-500 text-sm"> {error}</span>}
            </p>
        </div>
    );
};

export default ActiveSchedulesStatPanel;
