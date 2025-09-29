import {FC, useState, useEffect} from 'react';

interface HealthStatus {
    status: string;
}

const ServerHealthPanel: FC = () => {

    const [status, setStatus] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchHealthStatus = async () => {
            try {
                const response = await fetch('/actuator/health');

                if (!response.ok) {
                    setStatus('DOWN');
                    setError('Failed to fetch health');
                } else {
                    const data: HealthStatus = await response.json();
                    setStatus(data.status);
                }
            } catch (err) {
                setStatus('DOWN');
                setError('Failed to fetch health');
            }
        };

        void fetchHealthStatus();

        // Optional: Poll every 30 seconds
        const intervalId = setInterval(fetchHealthStatus, 30000);

        return () => clearInterval(intervalId);
    }, []);

    const isHealthy = status === 'UP';
    const displayStatus = status ? (isHealthy ? 'Healthy' : 'Unhealthy') : 'Loading...';

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-gray-500 text-sm">Server Health</h3>
            <p className={`text-2xl font-bold ${isHealthy ? 'text-green-500' : 'text-red-500'}`}>
                {displayStatus}
                {error && <span className="text-red-500 text-sm block">{error}</span>}
            </p>
        </div>
    );
};

export default ServerHealthPanel;
