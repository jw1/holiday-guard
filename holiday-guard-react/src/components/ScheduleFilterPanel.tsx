import React from 'react';
import {Schedule} from '../types/schedule';
import {CalendarFilters} from '../types/calendar-view';

interface ScheduleFilterPanelProps {
    schedules: Schedule[];
    filters: CalendarFilters;
    onFiltersChange: (filters: CalendarFilters) => void;
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
}

const ScheduleFilterPanel: React.FC<ScheduleFilterPanelProps> = ({
                                                                     schedules,
                                                                     filters,
                                                                     onFiltersChange,
                                                                     isOpen
                                                                 }) => {
    const handleScheduleToggle = (scheduleId: number) => {
        const newSelected = filters.selectedScheduleIds.includes(scheduleId)
            ? filters.selectedScheduleIds.filter(id => id !== scheduleId)
            : [...filters.selectedScheduleIds, scheduleId];

        onFiltersChange({
            ...filters,
            selectedScheduleIds: newSelected
        });
    };

    const handleSelectAll = () => {
        onFiltersChange({
            ...filters,
            selectedScheduleIds: schedules.map(s => s.id)
        });
    };

    const handleDeselectAll = () => {
        onFiltersChange({
            ...filters,
            selectedScheduleIds: []
        });
    };

    const handleStatusToggle = (status: keyof Omit<CalendarFilters, 'selectedScheduleIds'>) => {
        onFiltersChange({
            ...filters,
            [status]: !filters[status]
        });
    };

    const handleSelectAllStatuses = () => {
        onFiltersChange({
            ...filters,
            showRun: true,
            showNoRun: true,
            showForceRun: true,
            showSkip: true
        });
    };

    const handleClearAllStatuses = () => {
        onFiltersChange({
            ...filters,
            showRun: false,
            showNoRun: false,
            showForceRun: false,
            showSkip: false
        });
    };

    const panelClasses = `
        bg-white border-r border-gray-200 p-6 overflow-y-auto w-64
        transform transition-transform duration-300 ease-in-out
        absolute md:static md:translate-x-0 z-20
        ${isOpen ? 'translate-x-0' : '-translate-x-full'}
    `;

    return (
        <div className={panelClasses}>
            <h2 className="text-lg font-bold text-gray-800 mb-4">Filters</h2>

            {/* Schedule Selection */}
            <div className="mb-6">
                <div className="flex items-center justify-between mb-2">
                    <h3 className="text-sm font-semibold text-gray-700">Schedules</h3>
                    <span className="text-xs text-gray-500">
                        {filters.selectedScheduleIds.length}/{schedules.length}
                    </span>
                </div>

                <div className="flex gap-2 mb-3">
                    <button
                        onClick={handleSelectAll}
                        className="flex-1 text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                    >
                        Select All
                    </button>
                    <button
                        onClick={handleDeselectAll}
                        className="flex-1 text-xs px-2 py-1 bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                    >
                        Clear
                    </button>
                </div>

                <div className="space-y-2 max-h-96 overflow-y-auto">
                    {schedules.map(schedule => (
                        <label key={schedule.id}
                               className="flex items-center gap-2 cursor-pointer hover:bg-gray-50 p-2 rounded">
                            <input
                                type="checkbox"
                                checked={filters.selectedScheduleIds.includes(schedule.id)}
                                onChange={() => handleScheduleToggle(schedule.id)}
                                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                            />
                            <span className="text-sm text-gray-800">{schedule.name}</span>
                        </label>
                    ))}
                </div>
            </div>

            {/* Status Filters */}
            <div className="border-t border-gray-200 pt-4">
                <h3 className="text-sm font-semibold text-gray-700 mb-2">Show Status</h3>

                <div className="flex gap-2 mb-3">
                    <button
                        onClick={handleSelectAllStatuses}
                        className="flex-1 text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                    >
                        Select All
                    </button>
                    <button
                        onClick={handleClearAllStatuses}
                        className="flex-1 text-xs px-2 py-1 bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                    >
                        Clear
                    </button>
                </div>

                <div className="space-y-2">
                    <label className="flex items-center gap-2 cursor-pointer hover:bg-gray-50 p-2 rounded">
                        <input
                            type="checkbox"
                            checked={filters.showRun}
                            onChange={() => handleStatusToggle('showRun')}
                            className="rounded border-gray-300 text-green-600 focus:ring-green-500"
                        />
                        <div className="w-4 h-4 bg-green-100 border border-green-200 rounded"></div>
                        <span className="text-sm text-gray-800">Run Days</span>
                    </label>

                    <label className="flex items-center gap-2 cursor-pointer hover:bg-gray-50 p-2 rounded">
                        <input
                            type="checkbox"
                            checked={filters.showNoRun}
                            onChange={() => handleStatusToggle('showNoRun')}
                            className="rounded border-gray-300 text-red-600 focus:ring-red-500"
                        />
                        <div className="w-4 h-4 bg-red-100 border border-red-200 rounded"></div>
                        <span className="text-sm text-gray-800">No-Run Days</span>
                    </label>

                    <label className="flex items-center gap-2 cursor-pointer hover:bg-gray-50 p-2 rounded">
                        <input
                            type="checkbox"
                            checked={filters.showForceRun}
                            onChange={() => handleStatusToggle('showForceRun')}
                            className="rounded border-gray-300 text-green-600 focus:ring-green-500"
                        />
                        <div className="w-4 h-4 bg-green-600 rounded"></div>
                        <span className="text-sm text-gray-800">Force Run</span>
                    </label>

                    <label className="flex items-center gap-2 cursor-pointer hover:bg-gray-50 p-2 rounded">
                        <input
                            type="checkbox"
                            checked={filters.showSkip}
                            onChange={() => handleStatusToggle('showSkip')}
                            className="rounded border-gray-300 text-red-600 focus:ring-red-500"
                        />
                        <div className="w-4 h-4 bg-red-600 rounded"></div>
                        <span className="text-sm text-gray-800">Skip</span>
                    </label>
                </div>
            </div>
        </div>
    );
};

export default ScheduleFilterPanel;
