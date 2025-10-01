import {FC, useState, useEffect} from 'react';
import {getActiveSchedulesCount} from '../services/backend';

const ActiveSchedulesStatPanel: FC = () => {

    const [count, setCount] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchActiveCount = async () => {
            try {
                const activeCount = await getActiveSchedulesCount();
                setCount(activeCount);
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
