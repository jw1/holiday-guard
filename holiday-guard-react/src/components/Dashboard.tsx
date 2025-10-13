

import ActiveSchedulesPanel from './ActiveSchedulesPanel';
import TotalSchedulesStatPanel from './TotalSchedulesStatPanel';
import ActiveSchedulesStatPanel from './ActiveSchedulesStatPanel';
import ServerHealthPanel from './ServerHealthPanel';
import ManualQueryPanel from './ManualQueryPanel';

const Dashboard = () => {
  return (
    <main className="flex-1 p-8 bg-gray-50">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <TotalSchedulesStatPanel />
        <ActiveSchedulesStatPanel />
        <ServerHealthPanel />
      </div>

      <ActiveSchedulesPanel />

      <ManualQueryPanel />

      {/* Future enhancement: Add audit log viewer panel showing recent schedule queries and modifications */}
    </main>
  );
};

export default Dashboard;
