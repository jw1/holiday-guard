import React, { useState, useEffect, useMemo } from 'react';
import { Calendar, momentLocalizer, View } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import '../styles/calendar.css';
import { CalendarEvent, CalendarFilters, CalendarDay } from '../types/calendar-view';
import { getMultiScheduleCalendar, getAllSchedules } from '../services/backend';
import { Schedule } from '../types/schedule';
import ScheduleFilterPanel from './ScheduleFilterPanel';

const localizer = momentLocalizer(moment);

const ScheduleViewer: React.FC = () => {
    const [view, setView] = useState<View>('month');
    const [currentDate, setCurrentDate] = useState<Date>(new Date());
    const [allSchedules, setAllSchedules] = useState<Schedule[]>([]);
    const [calendarDays, setCalendarDays] = useState<CalendarDay[]>([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState<CalendarFilters>({
        selectedScheduleIds: [],
        showRun: true,
        showNoRun: true,
        showForceRun: true,
        showSkip: true,
    });

    // Fetch all schedules on mount
    useEffect(() => {
        getAllSchedules()
            .then(rawSchedules => {
                // Convert to Schedule format
                const schedules: Schedule[] = rawSchedules
                    .filter(s => s.active) // Only show active schedules
                    .map(s => ({
                        id: s.id,
                        name: s.name,
                        description: s.description,
                        country: s.country,
                        active: s.active,
                        createdAt: new Date(s.createdAt).toLocaleDateString(),
                        ruleType: s.ruleType,
                        ruleConfig: s.ruleConfig
                    }));
                setAllSchedules(schedules);
                // Select all schedules by default
                setFilters(prev => ({
                    ...prev,
                    selectedScheduleIds: schedules.map(s => s.id)
                }));
            })
            .catch(error => console.error('Error fetching schedules:', error));
    }, []);

    // Fetch calendar data when filters or date changes
    useEffect(() => {
        if (filters.selectedScheduleIds.length === 0) {
            setCalendarDays([]);
            setLoading(false);
            return;
        }

        setLoading(true);
        const yearMonth = moment(currentDate).format('YYYY-MM');

        getMultiScheduleCalendar(filters.selectedScheduleIds, yearMonth, true)
            .then(data => {
                setCalendarDays(data.days);
                setLoading(false);
            })
            .catch(error => {
                console.error('Error fetching calendar data:', error);
                setLoading(false);
            });
    }, [filters.selectedScheduleIds, currentDate]);

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
                const date = new Date(day.date);
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

        return (
            <div title={reason ? `${event.title} - ${status}\nReason: ${reason}` : `${event.title} - ${status}`}>
                {event.title}
            </div>
        );
    };

    return (
        <div className="flex bg-gray-100 min-h-screen">
            {/* Left Sidebar - Filters */}
            <ScheduleFilterPanel
                schedules={allSchedules}
                filters={filters}
                onFiltersChange={setFilters}
            />

            {/* Main Calendar Area */}
            <div className="flex-1 p-6">
                <div className="bg-white rounded-lg shadow p-6">
                    {/* Legend */}
                    <div className="flex items-center gap-4 mb-4 text-sm">
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
                    </div>

                    {loading ? (
                        <div className="flex items-center justify-center h-96">
                            <div className="text-gray-500">Loading calendar data...</div>
                        </div>
                    ) : (
                        <div style={{ height: '700px' }}>
                            <Calendar
                                localizer={localizer}
                                events={events}
                                startAccessor="start"
                                endAccessor="end"
                                view={view}
                                onView={setView}
                                date={currentDate}
                                onNavigate={setCurrentDate}
                                eventPropGetter={eventStyleGetter}
                                components={{
                                    event: EventComponent
                                }}
                                views={['month', 'agenda']}
                            />
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ScheduleViewer;
