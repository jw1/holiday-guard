import {FC} from 'react';
import {useActiveSchedulesCount} from '../hooks/queries';

const ActiveSchedulesStatPanel: FC = () => {
    const {data: count, isLoading, error} = useActiveSchedulesCount();

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-gray-500 text-sm">Active Schedules</h3>
            <p className="text-2xl font-bold">
                {isLoading ? 'Loading...' : count ?? 0}
                {error && <span className="text-red-500 text-sm"> Failed to fetch</span>}
            </p>
        </div>
    );
};

export default ActiveSchedulesStatPanel;
