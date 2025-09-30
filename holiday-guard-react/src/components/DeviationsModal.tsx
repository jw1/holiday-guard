import {FC, useState, useEffect} from 'react';
import {Schedule} from '@/types/schedule';
import DeviationCalendar from './DeviationCalendar';

interface DeviationsModalProps {
    schedule: Schedule | null;
    onClose: () => void;
    onSave: (deviations: { [date: string]: 'FORCE_RUN' | 'SKIP' }) => void;
}

const DeviationsModal: FC<DeviationsModalProps> = ({schedule, onClose, onSave}) => {

    const [deviations, setDeviations] = useState<{ [date: string]: 'FORCE_RUN' | 'SKIP' }>({});
    const [baseCalendar, setBaseCalendar] = useState<{ [date: string]: 'run' | 'no-run' }>({});

    useEffect(() => {
        if (schedule) {

            // Fetch deviations for the schedule
            fetch(`/api/v1/schedules/${schedule.id}/deviations`)
                .then(res => res.json())
                .then(data => {
                    const formattedDeviations = data.reduce((acc: any, deviation: any) => {
                        acc[deviation.date] = deviation.type;
                        return acc;
                    }, {});
                    setDeviations(formattedDeviations);
                })
                .catch(error => console.error('Error fetching deviations:', error));

            // Fetch base calendar data
            const yearMonth = new Date().toISOString().slice(0, 7); // Use current month for now
            fetch(`/api/v1/schedules/${schedule.id}/calendar?yearMonth=${yearMonth}`)
                .then(res => res.json())
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
    }, [schedule]);

    if (!schedule) return null;

    const handleDeviationsChange = (newDeviations: { [date: string]: 'FORCE_RUN' | 'SKIP' }) => {
        setDeviations(newDeviations);
    };

    const handleSave = () => {
        if (schedule) {
            const payload = {
                rules: [
                    {
                        ruleType: schedule.ruleType,
                        ruleConfig: schedule.ruleConfig,
                        effectiveFrom: new Date().toISOString().split('T')[0],
                        active: true,
                    },
                ],
                deviations: Object.entries(deviations).map(([date, type]) => ({
                    overrideDate: date,
                    action: type.toUpperCase(),
                    reason: 'User override',
                })),
            };

            fetch(`/api/v1/schedules/${schedule.id}/versions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            })
                .then(res => {
                    if (!res.ok) {
                        throw new Error('Save failed');
                    }
                    onSave(deviations);
                })
                .catch(error => {
                    console.error('Error saving deviations:', error);
                    window.alert('Failed to save deviations. Please try again.');
                });
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
            <div className="w-full max-w-4xl p-8 bg-white rounded-lg shadow-xl">
                <h2 className="mb-4 text-2xl font-bold">Manage Deviations for {schedule.name}</h2>

                <DeviationCalendar
                    baseCalendar={baseCalendar}
                    initialDeviations={deviations}
                    onDeviationsChange={handleDeviationsChange}
                />

                <div className="flex justify-end mt-6">
                    <button onClick={onClose}
                            className="px-4 py-2 mr-2 font-bold text-gray-700 bg-gray-200 rounded hover:bg-gray-300">
                        Cancel
                    </button>

                    <button onClick={handleSave}
                            className="px-4 py-2 font-bold text-white bg-blue-500 rounded hover:bg-blue-700">
                        Save
                    </button>
                </div>

            </div>
        </div>
    );
};

export default DeviationsModal;
