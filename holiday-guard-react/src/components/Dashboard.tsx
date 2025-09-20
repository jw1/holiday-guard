

const Dashboard = () => {
  return (
    <main className="flex-1 p-8 bg-gray-50">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {/* Stat Cards */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Total Users</h3>
          <p className="text-2xl font-bold">10,240</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Total Schedules</h3>
          <p className="text-2xl font-bold">89</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Active Schedules</h3>
          <p className="text-2xl font-bold">75</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Server Health</h3>
          <p className="text-2xl font-bold text-green-500">Healthy</p>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-xl font-bold mb-4">Recent Activity</h3>
        <table className="w-full">
          <thead>
            <tr className="text-left text-gray-500">
              <th className="pb-2">User</th>
              <th className="pb-2">Action</th>
              <th className="pb-2">Timestamp</th>
            </tr>
          </thead>
          <tbody>
            <tr className="border-t">
              <td className="py-3">James</td>
              <td className="py-3">Created new schedule "Payroll"</td>
              <td className="py-3">2025-09-12 10:30 AM</td>
            </tr>
            <tr className="border-t bg-gray-50">
              <td className="py-3">Sarah</td>
              <td className="py-3">Updated schedule "ACH"</td>
              <td className="py-3">2025-09-12 09:45 AM</td>
            </tr>
            <tr className="border-t">
              <td className="py-3">Mike</td>
              <td className="py-3">Queried schedule "Billing"</td>
              <td className="py-3">2025-09-12 09:15 AM</td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  );
};

export default Dashboard;
