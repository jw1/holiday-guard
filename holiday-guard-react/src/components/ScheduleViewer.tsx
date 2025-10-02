import React, { useState, useEffect, useMemo } from 'react';
import { Calendar, momentLocalizer, View } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import '../styles/calendar.css';
import { CalendarEvent, CalendarFilters, CalendarDay } from '../types/calendar-view';
import { Schedule } from '../types/schedule';
import ScheduleFilterPanel from './ScheduleFilterPanel';
import { exportToCSV, exportToICS } from '../utils/exportUtils';
import { parseISODateString } from '../utils/dateUtils';
import useWindowWidth from "../hooks/useWindowWidth";
import {Bars3Icon} from "@heroicons/react/24/outline";
import { useActiveSchedules, useMultiScheduleCalendar } from '../hooks/queries';

const localizer = momentLocalizer(moment);

const ScheduleViewer: React.FC = () => {
    const windowWidth = useWindowWidth();
    const isMobile = windowWidth < 768;

    // Set initial view based on screen size
    const [view, setView] = useState<View>(() => {
        return windowWidth < 768 ? 'agenda' : 'month';
    });
    const [currentDate, setCurrentDate] = useState<Date>(new Date());
    const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null);
    const [isFilterPanelOpen, setFilterPanelOpen] = useState(false);

    const [filters, setFilters] = useState<CalendarFilters>({
        selectedScheduleIds: [],
        showRun: true,
        showNoRun: true,
        showForceRun: true,
        showSkip: true,
    });

    // Fetch all active schedules
    const {data: allSchedules = []} = useActiveSchedules();

    // Fetch calendar data
    const yearMonth = moment(currentDate).format('YYYY-MM');
    const {data: calendarData, isLoading: loading} = useMultiScheduleCalendar(
        filters.selectedScheduleIds,
        yearMonth,
        true
    );

    const calendarDays = calendarData?.days || [];

    // Auto-switch to agenda view on narrow screens
    useEffect(() => {
        if (isMobile && view === 'month') {
            // Switch to agenda when screen becomes too narrow
            setView('agenda');
        }
        // Note: We don't auto-switch back to month when screen becomes wider
        // to respect user's choice if they manually selected agenda on desktop
    }, [isMobile, view]);

    // Select all schedules by default when they load
    useEffect(() => {
        if (allSchedules.length > 0 && filters.selectedScheduleIds.length === 0) {
            setFilters(prev => ({
                ...prev,
                selectedScheduleIds: allSchedules.map(s => s.id)
            }));
        }
    }, [allSchedules, filters.selectedScheduleIds.length]);

    // Convert calendar days to react-big-calendar events
    const events = useMemo(() => {
        return calendarDays
            .filter(day => {
                // Apply status filters
                if (day.status === 'run' && !filters.showRun) return false;
                if (day.status === 'no-run' && !filters.showNoRun) return false;
                if (day.status === 'FORCE_RUN' && !filters.showForceRun) return false;
                if (day.status === 'SKIP' && !filters.showSkip) return false;
                return true;
            })
            .map(day => {
                const date = parseISODateString(day.date);
                return {
                    title: day.scheduleName,
                    start: date,
                    end: date,
                    allDay: true,
                    resource: {
                        scheduleId: day.scheduleId,
                        scheduleName: day.scheduleName,
                        status: day.status,
                        reason: day.reason,
                    }
                } as CalendarEvent;
            });
    }, [calendarDays, filters]);

    // Custom event styling based on status
    const eventStyleGetter = (event: CalendarEvent) => {
        const status = event.resource?.status || 'run';
        let style: React.CSSProperties = {
            borderRadius: '4px',
            padding: '2px 4px',
            fontSize: '0.75rem',
            border: 'none', // No border by default
        };

        switch (status) {
            case 'run':
                style.backgroundColor = '#dcfce7'; // green-100
                style.color = '#166534'; // green-800
                break;
            case 'no-run':
                style.backgroundColor = '#fee2e2'; // red-100
                style.color = '#991b1b'; // red-800
                break;
            case 'FORCE_RUN':
                style.backgroundColor = '#16a34a'; // green-600 (darker green)
                style.color = '#ffffff'; // white text
                break;
            case 'SKIP':
                style.backgroundColor = '#dc2626'; // red-600 (darker red)
                style.color = '#ffffff'; // white text
                break;
        }

        return { style };
    };

    // Custom tooltip for events
    const EventComponent = ({ event }: { event: CalendarEvent }) => {
        const status = event.resource?.status;
        const reason = event.resource?.reason;

        // Format status for display
        const statusDisplay = status === 'FORCE_RUN' ? 'Force Run' :
                            status === 'SKIP' ? 'Skip' :
                            status === 'run' ? 'Run' : 'No-Run';

        // Create detailed tooltip
        const tooltipText = reason
            ? `${event.title}\nStatus: ${statusDisplay}\nReason: ${reason}`
            : `${event.title}\nStatus: ${statusDisplay}`;

        return (
            <div
                title={tooltipText}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    width: '100%',
                    overflow: 'hidden'
                }}
            >
                <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {event.title}
                </span>
                {reason && (
                    <span
                        style={{
                            fontSize: '0.7rem',
                            opacity: 0.8,
                            flexShrink: 0
                        }}
                        title={`Reason: ${reason}`}
                    >
                        ℹ️
                    </span>
                )}
            </div>
        );
    };

    // Export handlers
    const handleExportCSV = () => {
        const filename = `schedule-export-${moment(currentDate).format('YYYY-MM')}.csv`;
        exportToCSV(events, filename);
    };

    const handleExportICS = () => {
        const filename = `schedule-export-${moment(currentDate).format('YYYY-MM')}.ics`;
        exportToICS(events, filename);
    };

    return (
        <div className="flex h-full">
            {/* Left Sidebar - Filters */}
            <ScheduleFilterPanel
                schedules={allSchedules}
                filters={filters}
                onFiltersChange={setFilters}
                isOpen={isFilterPanelOpen}
                setIsOpen={setFilterPanelOpen}
            />

            {/* Main Calendar Area */}
            <div className="flex-1 p-6 flex flex-col">
                <div className="bg-white rounded-lg shadow p-6 flex flex-col flex-grow">
                    {/* Filter Button (mobile only) */}
                    <div className="md:hidden mb-4">
                        <button
                            onClick={() => setFilterPanelOpen(true)}
                            className="flex items-center gap-2 px-4 py-2 text-sm bg-gray-600 text-white rounded hover:bg-gray-700"
                            title="Show Filters"
                        >
                            <Bars3Icon className="h-4 w-4" />
                            Filters
                        </button>
                    </div>

                    {/* Calendar */}
                    {loading ? (
                        <div className="flex items-center justify-center flex-grow">
                            <div className="text-gray-500">Loading calendar data...</div>
                        </div>
                    ) : filters.selectedScheduleIds.length === 0 ? (
                        <div className="flex flex-col items-center justify-center flex-grow text-gray-500">
                            <svg className="w-16 h-16 mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            <p className="text-lg font-medium">No Schedules Selected</p>
                            <p className="text-sm mt-2">Select one or more schedules from the sidebar to view the calendar</p>
                        </div>
                    ) : (
                        <div className="flex-grow mb-4">
                            <Calendar
                                localizer={localizer}
                                events={events}
                                startAccessor="start"
                                endAccessor="end"
                                view={view}
                                onView={setView}
                                date={currentDate}
                                onNavigate={setCurrentDate}
                                onSelectEvent={setSelectedEvent}
                                eventPropGetter={eventStyleGetter}
                                components={{
                                    event: EventComponent
                                }}
                                views={['month', 'agenda']}
                            />
                        </div>
                    )}

                    {/* Legend and Export Buttons - Bottom */}
                    {!loading && filters.selectedScheduleIds.length > 0 && (
                        <div className="border-t pt-4 mt-4">
                            <div className="flex items-center justify-between flex-wrap gap-4">
                                {/* Legend */}
                                <div className="flex items-center gap-4 text-sm flex-wrap">
                                    <div className="flex items-center gap-2">
                                        <div className="w-4 h-4 bg-green-100 border border-green-200 rounded"></div>
                                        <span>Run</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <div className="w-4 h-4 bg-red-100 border border-red-200 rounded"></div>
                                        <span>No-Run</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <div className="w-4 h-4 bg-green-600 rounded"></div>
                                        <span>Force Run</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <div className="w-4 h-4 bg-red-600 rounded"></div>
                                        <span>Skip</span>
                                    </div>
                                    <div className="flex items-center gap-2 ml-4 border-l pl-4 border-gray-300">
                                        <span>ℹ️</span>
                                        <span className="text-gray-600 italic">Has deviation reason</span>
                                    </div>
                                </div>

                                {/* Export Buttons */}
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={handleExportCSV}
                                        disabled={events.length === 0}
                                        className="flex items-center gap-2 px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
                                        title="Export as CSV"
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                        </svg>
                                        <span className="hidden sm:inline">CSV</span>
                                    </button>
                                    <button
                                        onClick={handleExportICS}
                                        disabled={events.length === 0}
                                        className="flex items-center gap-2 px-4 py-2 text-sm bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
                                        title="Export as ICS (iCalendar)"
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                        </svg>
                                        <span className="hidden sm:inline">ICS</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Event Details Modal */}
            {selectedEvent && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={() => setSelectedEvent(null)}>
                    <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4" onClick={(e) => e.stopPropagation()}>
                        <div className="flex justify-between items-start mb-4">
                            <h3 className="text-lg font-bold text-gray-800">Event Details</h3>
                            <button
                                onClick={() => setSelectedEvent(null)}
                                className="text-gray-400 hover:text-gray-600"
                            >
                                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <label className="text-sm font-semibold text-gray-600">Schedule</label>
                                <p className="text-gray-800 mt-1">{selectedEvent.resource?.scheduleName}</p>
                            </div>

                            <div>
                                <label className="text-sm font-semibold text-gray-600">Date</label>
                                <p className="text-gray-800 mt-1">{moment(selectedEvent.start).format('MMMM D, YYYY')}</p>
                            </div>

                            <div>
                                <label className="text-sm font-semibold text-gray-600">Status</label>
                                <div className="mt-1">
                                    {selectedEvent.resource?.status === 'FORCE_RUN' && (
                                        <span className="inline-flex items-center px-3 py-1 rounded text-sm font-medium bg-green-600 text-white">
                                            Force Run
                                        </span>
                                    )}
                                    {selectedEvent.resource?.status === 'SKIP' && (
                                        <span className="inline-flex items-center px-3 py-1 rounded text-sm font-medium bg-red-600 text-white">
                                            Skip
                                        </span>
                                    )}
                                    {selectedEvent.resource?.status === 'run' && (
                                        <span className="inline-flex items-center px-3 py-1 rounded text-sm font-medium bg-green-100 text-green-800">
                                            Run
                                        </span>
                                    )}
                                    {selectedEvent.resource?.status === 'no-run' && (
                                        <span className="inline-flex items-center px-3 py-1 rounded text-sm font-medium bg-red-100 text-red-800">
                                            No-Run
                                        </span>
                                    )}
                                </div>
                            </div>

                            {selectedEvent.resource?.reason && (
                                <div>
                                    <label className="text-sm font-semibold text-gray-600">Deviation Reason</label>
                                    <p className="text-gray-800 mt-1 bg-gray-50 p-3 rounded border border-gray-200">
                                        {selectedEvent.resource.reason}
                                    </p>
                                </div>
                            )}
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                onClick={() => setSelectedEvent(null)}
                                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Backdrop for mobile filter panel */}
            {isFilterPanelOpen && (
                <div
                    className="md:hidden fixed inset-0 bg-black opacity-50 z-10"
                    onClick={() => setFilterPanelOpen(false)}
                ></div>
            )}
        </div>
    );
};

export default ScheduleViewer;
