import React, { useState, useEffect } from 'react';
import { CronExpressionBuilderProps, CronBuilderState, DayOfMonthMode } from '../types/cron';
import { parseCronToState, stateToCron, cronToHumanReadable, validateCronState } from '../utils/cronUtils';

const CronExpressionBuilder: React.FC<CronExpressionBuilderProps> = ({ value, onChange, onValidationChange }) => {
    const [state, setState] = useState<CronBuilderState>(() => parseCronToState(value));

    // Update state when value prop changes (for editing existing schedules)
    useEffect(() => {
        setState(parseCronToState(value));
    }, [value]);

    // Emit changes when state updates
    useEffect(() => {
        const validation = validateCronState(state);
        onValidationChange(validation.isValid, validation.error);

        if (validation.isValid) {
            const cronExpression = stateToCron(state);
            onChange(cronExpression);
        }
    }, [state, onChange, onValidationChange]);

    // Month toggle handlers
    const toggleMonth = (month: string) => {
        setState(prev => {
            const months = prev.months.includes('*')
                ? [month]
                : prev.months.includes(month)
                    ? prev.months.filter(m => m !== month)
                    : [...prev.months, month];

            return { ...prev, months: months.length === 0 ? ['*'] : months };
        });
    };

    const toggleAllMonths = () => {
        setState(prev => ({
            ...prev,
            months: prev.months.includes('*') ? [] : ['*']
        }));
    };

    // Day of month handlers
    const setDayOfMonthMode = (mode: DayOfMonthMode) => {
        setState(prev => ({
            ...prev,
            dayOfMonthMode: mode,
            daysOfMonth: mode === 'every' ? ['*'] : mode === 'last' ? ['L'] : [],
            daysOfWeek: mode === 'every' ? prev.daysOfWeek : ['?']
        }));
    };

    const toggleDayOfMonth = (day: string) => {
        setState(prev => {
            const days = prev.daysOfMonth.includes(day)
                ? prev.daysOfMonth.filter(d => d !== day)
                : [...prev.daysOfMonth, day];

            return { ...prev, daysOfMonth: days.length === 0 ? ['*'] : days };
        });
    };

    const toggleAllDaysOfMonth = () => {
        const allDays = Array.from({ length: 31 }, (_, i) => String(i + 1));
        setState(prev => ({
            ...prev,
            daysOfMonth: prev.daysOfMonth.length === 31 ? [] : allDays
        }));
    };

    // Day of week handlers
    const toggleDayOfWeek = (day: string) => {
        setState(prev => {
            const isAll = prev.daysOfWeek.includes('*') || prev.daysOfWeek.includes('?');
            const days = isAll
                ? [day]
                : prev.daysOfWeek.includes(day)
                    ? prev.daysOfWeek.filter(d => d !== day)
                    : [...prev.daysOfWeek, day];

            return { ...prev, daysOfWeek: days.length === 0 ? ['*'] : days };
        });
    };

    const setDaysOfWeek = (days: string[]) => {
        setState(prev => ({ ...prev, daysOfWeek: days }));
    };

    const copyToClipboard = () => {
        const cronExpression = stateToCron(state);
        navigator.clipboard.writeText(cronExpression);
    };

    const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    const isMonthSelected = (month: number) => state.months.includes('*') || state.months.includes(String(month));
    const isDayOfMonthSelected = (day: number) => state.daysOfMonth.includes(String(day));
    const isDayOfWeekSelected = (day: number) => {
        const isAll = state.daysOfWeek.includes('*') || state.daysOfWeek.includes('?');
        return isAll || state.daysOfWeek.includes(String(day));
    };

    const dayOfWeekDisabled = state.dayOfMonthMode !== 'every';
    const cronExpression = stateToCron(state);
    const humanReadable = cronToHumanReadable(state);

    return (
        <div className="space-y-4">
            {/* Months Section */}
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <div className="flex items-center justify-between mb-3">
                    <label className="text-sm font-semibold text-gray-700">Months</label>
                    <button
                        type="button"
                        onClick={toggleAllMonths}
                        className="text-xs px-2 py-1 rounded bg-gray-200 hover:bg-gray-300 text-gray-700"
                    >
                        {state.months.includes('*') ? 'Deselect All' : 'All Months'}
                    </button>
                </div>
                <div className="grid grid-cols-4 sm:grid-cols-6 gap-2">
                    {monthNames.map((name, index) => (
                        <button
                            key={index}
                            type="button"
                            onClick={() => toggleMonth(String(index + 1))}
                            className={`px-3 py-2 text-sm rounded border transition-colors ${
                                isMonthSelected(index + 1)
                                    ? 'bg-blue-600 text-white border-blue-700'
                                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'
                            }`}
                        >
                            {name}
                        </button>
                    ))}
                </div>
            </div>

            {/* Day of Month Section */}
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <label className="text-sm font-semibold text-gray-700 block mb-3">Day of Month</label>

                <div className="space-y-3">
                    {/* Radio options */}
                    <div className="space-y-2">
                        <label className="flex items-center">
                            <input
                                type="radio"
                                checked={state.dayOfMonthMode === 'every'}
                                onChange={() => setDayOfMonthMode('every')}
                                className="mr-2"
                            />
                            <span className="text-sm text-gray-700">Every day (use day of week filter below)</span>
                        </label>
                        <label className="flex items-center">
                            <input
                                type="radio"
                                checked={state.dayOfMonthMode === 'specific'}
                                onChange={() => setDayOfMonthMode('specific')}
                                className="mr-2"
                            />
                            <span className="text-sm text-gray-700">Specific days of month</span>
                        </label>
                        <label className="flex items-center">
                            <input
                                type="radio"
                                checked={state.dayOfMonthMode === 'last'}
                                onChange={() => setDayOfMonthMode('last')}
                                className="mr-2"
                            />
                            <span className="text-sm text-gray-700">Last day of month</span>
                        </label>
                    </div>

                    {/* Day picker (only when specific mode) */}
                    {state.dayOfMonthMode === 'specific' && (
                        <div>
                            <div className="flex items-center justify-between mb-2">
                                <span className="text-xs text-gray-600">Select days:</span>
                                <button
                                    type="button"
                                    onClick={toggleAllDaysOfMonth}
                                    className="text-xs px-2 py-1 rounded bg-gray-200 hover:bg-gray-300 text-gray-700"
                                >
                                    {state.daysOfMonth.length === 31 ? 'Deselect All' : 'All Days'}
                                </button>
                            </div>
                            <div className="grid grid-cols-7 gap-1">
                                {Array.from({ length: 31 }, (_, i) => i + 1).map(day => (
                                    <button
                                        key={day}
                                        type="button"
                                        onClick={() => toggleDayOfMonth(String(day))}
                                        className={`px-2 py-1 text-xs rounded border transition-colors ${
                                            isDayOfMonthSelected(day)
                                                ? 'bg-blue-600 text-white border-blue-700'
                                                : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'
                                        }`}
                                    >
                                        {day}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Day of Week Section */}
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <div className="flex items-center justify-between mb-3">
                    <label className="text-sm font-semibold text-gray-700">Day of Week</label>
                    {!dayOfWeekDisabled && (
                        <div className="flex gap-2">
                            <button
                                type="button"
                                onClick={() => setDaysOfWeek(['*'])}
                                className="text-xs px-2 py-1 rounded bg-gray-200 hover:bg-gray-300 text-gray-700"
                            >
                                All
                            </button>
                            <button
                                type="button"
                                onClick={() => setDaysOfWeek(['1', '2', '3', '4', '5'])}
                                className="text-xs px-2 py-1 rounded bg-gray-200 hover:bg-gray-300 text-gray-700"
                            >
                                Weekdays
                            </button>
                            <button
                                type="button"
                                onClick={() => setDaysOfWeek(['0', '6'])}
                                className="text-xs px-2 py-1 rounded bg-gray-200 hover:bg-gray-300 text-gray-700"
                            >
                                Weekends
                            </button>
                        </div>
                    )}
                </div>
                <div className="grid grid-cols-7 gap-2">
                    {dayNames.map((name, index) => (
                        <button
                            key={index}
                            type="button"
                            onClick={() => !dayOfWeekDisabled && toggleDayOfWeek(String(index))}
                            disabled={dayOfWeekDisabled}
                            className={`px-3 py-2 text-sm rounded border transition-colors ${
                                dayOfWeekDisabled
                                    ? 'bg-gray-50 text-gray-400 border-gray-200 cursor-not-allowed'
                                    : isDayOfWeekSelected(index)
                                        ? 'bg-blue-600 text-white border-blue-700'
                                        : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'
                            }`}
                        >
                            {name}
                        </button>
                    ))}
                </div>
                {dayOfWeekDisabled && (
                    <p className="text-xs text-gray-500 mt-2 italic">
                        Day of week is disabled when specific days of month are selected
                    </p>
                )}
            </div>

            {/* Preview Section */}
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <div className="flex items-center justify-between mb-2">
                    <label className="text-sm font-semibold text-gray-700">Preview</label>
                    <button
                        type="button"
                        onClick={copyToClipboard}
                        className="text-xs px-2 py-1 rounded bg-gray-200 hover:bg-gray-300 text-gray-700"
                        title="Copy to clipboard"
                    >
                        Copy
                    </button>
                </div>
                <div className="bg-gray-900 text-green-400 font-mono text-sm p-3 rounded mb-2">
                    {cronExpression}
                </div>
                <p className="text-sm text-gray-700 italic">
                    {humanReadable}
                </p>
            </div>
        </div>
    );
};

export default CronExpressionBuilder;
