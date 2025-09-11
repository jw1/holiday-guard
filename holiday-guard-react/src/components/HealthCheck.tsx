import React, { useEffect, useState } from 'react';

interface HealthResponse {
  status: string;
  [key: string]: any; // Allow other properties
}

const HealthCheck: React.FC = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        const response = await fetch('/actuator/health');
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data: HealthResponse = await response.json();
        setHealth(data);
      } catch (e: any) {
        setError(e.message);
      }
    };

    fetchHealth();
  }, []);

  if (error) {
    return <div className="text-red-500">Error: {error}</div>;
  }

  if (!health) {
    return <div className="text-gray-500">Loading health status...</div>;
  }

  return (
    <div className="mt-8 p-4 border rounded shadow-md">
      <h2 className="text-2xl font-bold mb-4">Backend Health Status</h2>
      <pre className="bg-gray-100 p-2 rounded text-left text-sm">
        {JSON.stringify(health, null, 2)}
      </pre>
    </div>
  );
};

export default HealthCheck;
