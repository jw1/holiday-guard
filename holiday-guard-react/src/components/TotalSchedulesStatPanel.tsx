import {FC} from 'react';
import {useTotalSchedulesCount} from '../hooks/queries';

const TotalSchedulesStatPanel: FC = () => {
    const {data: count, isLoading, error} = useTotalSchedulesCount();

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-gray-500 text-sm">Total Schedules</h3>
            <p className="text-2xl font-bold">
                {isLoading ? 'Loading...' : count ?? 0}
                {error && <span className="text-red-500 text-sm"> Failed to fetch</span>}
            </p>
        </div>
    );
};

export default TotalSchedulesStatPanel;
