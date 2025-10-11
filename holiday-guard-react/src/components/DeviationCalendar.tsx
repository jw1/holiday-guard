import React, {useState, useEffect} from 'react';
import { RunStatus } from '../types/runStatus';

// Define the types for the calendar props
interface DeviationCalendarProps {
    baseCalendar: { [date: string]: RunStatus };
    initialDeviations: { [date: string]: { type: 'FORCE_RUN' | 'FORCE_SKIP', reason: string } };
    onDeviationsChange: (newDeviations: { [date: string]: { type: 'FORCE_RUN' | 'FORCE_SKIP', reason: string } }) => void;
    onMonthChange: (newMonth: Date) => void;
}

const DeviationCalendar: React.FC<DeviationCalendarProps> = ({
                                                                                 baseCalendar,
                                                                                 initialDeviations,
                                                                                 onDeviationsChange,
                                                                                 onMonthChange
                                                                             }) => {
    const [deviations, setDeviations] = useState(initialDeviations);
    const [currentDate, setCurrentDate] = useState(new Date());

    // Sync internal state when initialDeviations prop changes (only from parent)
    useEffect(() => {
        setDeviations(initialDeviations);
    }, [initialDeviations]);

    // Helper functions (similar to business-day-calendar.tsx)
    const formatDate = (date: Date) => date.toISOString().split('T')[0];

    const getDaysInMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();

    const getFirstDayOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth(), 1).getDay();

    const changeMonth = (delta: number) => {
        const newDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + delta, 1);
        setCurrentDate(newDate);
        onMonthChange(newDate);
    };

    const handleDayClick = (day: number) => {
        const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        const dateKey = formatDate(date);

        const newDeviations = {...deviations};
        if (newDeviations[dateKey]) {
            delete newDeviations[dateKey];
        } else {
            const deviationType = baseCalendar[dateKey] === RunStatus.RUN ? 'FORCE_SKIP' : 'FORCE_RUN';
            newDeviations[dateKey] = { type: deviationType, reason: '' };
        }

        setDeviations(newDeviations);
        // Notify parent of user changes (after state update, not during)
        onDeviationsChange(newDeviations);
    };

    const getDayState = (day: number) => {
        const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        const dateKey = formatDate(date);

        if (deviations[dateKey]) {
            return deviations[dateKey].type; // 'FORCE_RUN' or 'SKIP'
        }
        return baseCalendar[dateKey] || 'default'; // 'run', 'no-run', or 'default'
    };

    const generateCalendarDays = () => {
        const daysInMonth = getDaysInMonth(currentDate);
        const firstDay = getFirstDayOfMonth(currentDate);
        const days = [];
        for (let i = 0; i < firstDay; i++) {
            days.push(null);
        }
        for (let day = 1; day <= daysInMonth; day++) {
            days.push(day);
        }
        return days;
    };

    const getDayClasses = (state: string) => {
        const baseClasses = 'w-10 h-10 flex items-center justify-center rounded cursor-pointer transition-all duration-200';
        switch (state) {
            case RunStatus.RUN:
                return `${baseClasses} bg-green-100 text-green-800`;
            case RunStatus.SKIP:
                return `${baseClasses} bg-red-100 text-red-800`;
            case 'FORCE_RUN':
                return `${baseClasses} bg-green-600 text-white`;
            case 'FORCE_SKIP':
                return `${baseClasses} bg-red-600 text-white`;
            default:
                return `${baseClasses} bg-gray-100`;
        }
    };

    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

    return (
        <div className="p-4">
            <div className="flex items-center justify-between mb-4">
                <button onClick={() => changeMonth(-1)} className="px-3 py-1 bg-gray-200 rounded">‹</button>
                <h2 className="text-lg font-semibold">{monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}</h2>
                <button onClick={() => changeMonth(1)} className="px-3 py-1 bg-gray-200 rounded">›</button>
            </div>
            <div className="grid grid-cols-7 gap-1 mb-2 text-center text-sm font-medium text-gray-600">
                {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => <div key={day}>{day}</div>)}
            </div>
            <div className="grid grid-cols-7 gap-1">
                {generateCalendarDays().map((day, index) => (
                    <div key={index} className="flex justify-center">
                        {day ? (
                            <div className={getDayClasses(getDayState(day))} onClick={() => handleDayClick(day)}>
                                {day}
                            </div>
                        ) : <div className="w-10 h-10"></div>}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default DeviationCalendar;
