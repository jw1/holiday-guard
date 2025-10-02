import {FC} from 'react';
import {useScheduleStatus} from '../hooks/queries';

const ActiveSchedulesPanel: FC = () => {
    const {data: scheduleStatus = [], isLoading: loading, error} = useScheduleStatus();

    return (
        <div className="p-6 bg-white rounded-lg shadow mb-8">
            <h3 className="text-xl font-bold mb-4">Active Schedules - Run Status Today</h3>
            {loading && <p>Loading...</p>}
            {error && <p className="text-red-500">Failed to fetch schedule status</p>}
            {!loading && !error && (
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th scope="col"
                                className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                                Schedule Name
                            </th>
                            <th scope="col"
                                className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                                Should Run?
                            </th>
                            <th scope="col"
                                className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                                Reason
                            </th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {scheduleStatus.map((schedule) => (
                            <tr key={schedule.scheduleId}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{schedule.scheduleName}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${schedule.shouldRun ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {schedule.shouldRun ? 'Yes' : 'No'}
                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{schedule.reason}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default ActiveSchedulesPanel;
