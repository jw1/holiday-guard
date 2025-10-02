import {FC} from 'react';
import {useHealthStatus} from '../hooks/queries';

const ServerHealthPanel: FC = () => {
    const {data, isLoading, error} = useHealthStatus();

    const status = data?.status;
    const isHealthy = status === 'UP';
    const displayStatus = isLoading ? 'Loading...' : (status ? (isHealthy ? 'Healthy' : 'Unhealthy') : 'Unknown');

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-gray-500 text-sm">Server Health</h3>
            <p className={`text-2xl font-bold ${isHealthy ? 'text-green-500' : 'text-red-500'}`}>
                {displayStatus}
                {error && <span className="text-red-500 text-sm block">Failed to fetch health</span>}
            </p>
        </div>
    );
};

export default ServerHealthPanel;
