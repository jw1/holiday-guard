import React, {useState, useEffect} from 'react';

// Define the types for the calendar props
interface ScheduleOverridesCalendarProps {
    baseCalendar: { [date: string]: 'run' | 'no-run' };
    initialOverrides: { [date: string]: 'FORCE_RUN' | 'SKIP' };
    onOverridesChange: (newOverrides: { [date: string]: 'FORCE_RUN' | 'SKIP' }) => void;
}

const ScheduleOverridesCalendar: React.FC<ScheduleOverridesCalendarProps> = ({
                                                                                 baseCalendar,
                                                                                 initialOverrides,
                                                                                 onOverridesChange
                                                                             }) => {
    const [overrides, setOverrides] = useState(initialOverrides);
    const [currentDate, setCurrentDate] = useState(new Date());

    useEffect(() => {
        onOverridesChange(overrides);
    }, [overrides, onOverridesChange]);

    // Helper functions (similar to business-day-calendar.tsx)
    const formatDate = (date: Date) => date.toISOString().split('T')[0];

    const getDaysInMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();

    const getFirstDayOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth(), 1).getDay();

    const changeMonth = (delta: number) => {
        setCurrentDate(prev => new Date(prev.getFullYear(), prev.getMonth() + delta, 1));
    };

    const handleDayClick = (day: number) => {
        const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        const dateKey = formatDate(date);

        setOverrides(prev => {
            const newOverrides = {...prev};
            if (newOverrides[dateKey]) {
                delete newOverrides[dateKey];
            } else {
                newOverrides[dateKey] = baseCalendar[dateKey] === 'run' ? 'SKIP' : 'FORCE_RUN';
            }
            return newOverrides;
        });
    };

    const getDayState = (day: number) => {
        const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        const dateKey = formatDate(date);

        if (overrides[dateKey]) {
            return overrides[dateKey]; // 'FORCE_RUN' or 'SKIP'
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
            case 'run':
                return `${baseClasses} bg-green-100 text-green-800`;
            case 'no-run':
                return `${baseClasses} bg-red-100 text-red-800`;
            case 'FORCE_RUN':
                return `${baseClasses} bg-green-200 text-green-900 ring-2 ring-green-500`;
            case 'SKIP':
                return `${baseClasses} bg-red-200 text-red-900 ring-2 ring-red-500`;
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

export default ScheduleOverridesCalendar;
