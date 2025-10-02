import {FC, useState, useEffect} from 'react';
import {Schedule} from '@/types/schedule';
import DeviationCalendar from './DeviationCalendar';
import {getScheduleDeviations, getScheduleCalendar, VersionPayload} from '../services/backend';

interface DeviationsModalProps {
    schedule: Schedule | null;
    onClose: () => void;
    // The onSave prop will now pass the entire payload up to the parent.
    onSave: (payload: VersionPayload) => void;
}

const DeviationsModal: FC<DeviationsModalProps> = ({schedule, onClose, onSave}) => {

    const [deviations, setDeviations] = useState<{ [date: string]: 'FORCE_RUN' | 'SKIP' }>({});
    const [baseCalendar, setBaseCalendar] = useState<{ [date: string]: 'run' | 'no-run' }>({});
    const [currentMonth, setCurrentMonth] = useState<Date>(new Date());

    useEffect(() => {
        if (schedule) {
            getScheduleDeviations(schedule.id)
                .then(data => {
                    console.log('Received deviations from backend:', data);
                    const formattedDeviations = data.reduce((acc: any, deviation: any) => {
                        console.log('Processing deviation:', deviation);
                        acc[deviation.date] = deviation.type;
                        return acc;
                    }, {});
                    console.log('Formatted deviations:', formattedDeviations);
                    setDeviations(formattedDeviations);
                })
                .catch(error => console.error('Error fetching deviations:', error));
        }
    }, [schedule]);

    useEffect(() => {
        if (schedule) {
            const yearMonth = currentMonth.toISOString().slice(0, 7);
            getScheduleCalendar(schedule.id, yearMonth)
                .then(data => {
                    const calendar = Object.entries(data.days).reduce((acc: any, [day, status]) => {
                        const date = `${data.yearMonth}-${String(day).padStart(2, '0')}`;
                        acc[date] = status;
                        return acc;
                    }, {});
                    setBaseCalendar(calendar);
                })
                .catch(error => console.error('Error fetching base calendar:', error));
        }
    }, [schedule, currentMonth]);

    if (!schedule) return null;

    const handleDeviationsChange = (newDeviations: { [date: string]: 'FORCE_RUN' | 'SKIP' }) => {
        setDeviations(newDeviations);
    };

    const handleMonthChange = (newMonth: Date) => {
        setCurrentMonth(newMonth);
    };

    const handleSave = () => {
        if (schedule) {
            // 1. Construct the payload with correct structure (singular 'rule', not 'rules' array)
            const payload: VersionPayload = {
                rule: {
                    ruleType: schedule.ruleType,
                    ruleConfig: schedule.ruleConfig,
                    effectiveFrom: new Date().toISOString().split('T')[0],
                    active: true,
                },
                deviations: Object.entries(deviations).map(([date, type]) => ({
                    deviationDate: date,
                    action: type.toUpperCase(),
                    reason: 'User-created deviation', // Default reason - can be updated via right-click later
                })),
            };

            // 2. Pass the payload to the parent component instead of making the API call here.
            onSave(payload);
        }
    };

    const deviationCount = Object.keys(deviations).length;
    const sortedDeviations = Object.entries(deviations).sort(([dateA], [dateB]) =>
        dateA.localeCompare(dateB)
    );

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
            <div className="w-full max-w-6xl p-8 bg-white rounded-lg shadow-xl">
                <h2 className="mb-4 text-2xl font-bold">Manage Deviations for {schedule.name}</h2>

                <div className="flex gap-6">
                    {/* Left side - Calendar */}
                    <div className="flex-1">
                        <DeviationCalendar
                            baseCalendar={baseCalendar}
                            initialDeviations={deviations}
                            onDeviationsChange={handleDeviationsChange}
                            onMonthChange={handleMonthChange}
                        />
                    </div>

                    {/* Right side - Deviation Summary */}
                    <div className="w-80 border-l pl-6">
                        <div className="mb-4">
                            <div className="flex items-center justify-between">
                                <h3 className="text-lg font-bold text-gray-700">Current Deviations</h3>
                                <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm font-semibold">
                                    {deviationCount}
                                </span>
                            </div>
                        </div>

                        <div className="max-h-96 overflow-y-auto">
                            {deviationCount === 0 ? (
                                <p className="text-gray-500 text-sm italic">No deviations configured</p>
                            ) : (
                                <div className="space-y-0">
                                    {sortedDeviations.map(([date, type]) => (
                                        <div key={date} className="border-b py-3 last:border-b-0">
                                            <div className="flex items-center justify-between mb-1">
                                                <span className="text-sm font-medium text-gray-700">
                                                    {(() => {
                                                        const [year, month, day] = date.split('-').map(Number);
                                                        return new Date(year, month - 1, day).toLocaleDateString('en-US', {
                                                            year: 'numeric',
                                                            month: 'short',
                                                            day: 'numeric'
                                                        });
                                                    })()}
                                                </span>
                                                <span className={`px-2 py-1 rounded text-xs font-semibold ${
                                                    type === 'FORCE_RUN'
                                                        ? 'bg-green-100 text-green-800'
                                                        : 'bg-red-100 text-red-800'
                                                }`}>
                                                    {type === 'FORCE_RUN' ? 'Force Run' : 'Skip'}
                                                </span>
                                            </div>
                                            <p className="text-xs text-gray-500 italic">
                                                User-created deviation
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <div className="flex justify-end mt-6">
                    <button onClick={onClose}
                            className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded mr-2">
                        Cancel
                    </button>

                    <button onClick={handleSave}
                            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                        Save
                    </button>
                </div>

            </div>
        </div>
    );
};

export default DeviationsModal;