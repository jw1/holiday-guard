import React, { useState } from 'react';

const BusinessDayCalendar = () => {
  // Sample business days and holidays (you'd fetch these from your API)
  const businessDays = [
    '2024-01-02', '2024-01-03', '2024-01-04', '2024-01-05', '2024-01-08',
    '2024-01-09', '2024-01-10', '2024-01-11', '2024-01-12', '2024-01-16',
    '2024-01-17', '2024-01-18', '2024-01-19', '2024-01-22', '2024-01-23',
    '2024-01-24', '2024-01-25', '2024-01-26', '2024-01-29', '2024-01-30',
    '2024-01-31',
  ];

  const holidays = [
    '2024-01-01', // New Year's Day
    '2024-01-15', // MLK Day
    '2024-01-20', // Weekend but marked as holiday for demo
    '2024-01-21', // Weekend but marked as holiday for demo
  ];

  // State to track overrides and current date
  const [overrides, setOverrides] = useState({
    '2024-01-01': 'business', // New Year overridden to business day
    '2024-01-03': 'holiday',  // Regular business day overridden to holiday
  });

  const [currentDate, setCurrentDate] = useState(new Date(2024, 0, 1)); // January 2024

  // Helper functions
  const formatDate = (date) => {
    return date.toISOString().split('T')[0];
  };

  const getDaysInMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  const getFirstDayOfMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  };

  const changeMonth = (delta) => {
    setCurrentDate(prev => new Date(prev.getFullYear(), prev.getMonth() + delta, 1));
  };

  const changeYear = (delta) => {
    setCurrentDate(prev => new Date(prev.getFullYear() + delta, prev.getMonth(), 1));
  };

  // Handle double click to toggle override
  const handleDayDoubleClick = (day) => {
    const dayDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
    const dateKey = formatDate(dayDate);
    const isOriginallyBusinessDay = businessDays.includes(dateKey);
    
    setOverrides(prev => {
      if (prev[dateKey]) {
        // Remove override
        const { [dateKey]: removed, ...rest } = prev;
        return rest;
      } else {
        // Add override (toggle the current state)
        return {
          ...prev,
          [dateKey]: isOriginallyBusinessDay ? 'holiday' : 'business'
        };
      }
    });
  };

  // Get day state
  const getDayState = (day) => {
    const dayDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
    const dateKey = formatDate(dayDate);
    
    if (overrides[dateKey] === 'business') return 'overriddenToBusiness';
    if (overrides[dateKey] === 'holiday') return 'overriddenToHoliday';
    if (businessDays.includes(dateKey)) return 'businessDay';
    if (holidays.includes(dateKey)) return 'holiday';
    return 'normal';
  };

  // Generate calendar days
  const generateCalendarDays = () => {
    const daysInMonth = getDaysInMonth(currentDate);
    const firstDay = getFirstDayOfMonth(currentDate);
    const days = [];

    // Empty cells for days before the first day of the month
    for (let i = 0; i < firstDay; i++) {
      days.push(null);
    }

    // Days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      days.push(day);
    }

    return days;
  };

  const getDayClasses = (state) => {
    const baseClasses = 'w-10 h-10 flex items-center justify-center rounded cursor-pointer transition-all duration-200 hover:scale-105';
    
    switch (state) {
      case 'businessDay':
        return `${baseClasses} bg-green-100 text-green-800 hover:bg-green-200`;
      case 'holiday':
        return `${baseClasses} bg-red-100 text-red-800 hover:bg-red-200`;
      case 'overriddenToBusiness':
        return `${baseClasses} bg-green-200 text-green-900 ring-2 ring-green-400 hover:bg-green-300`;
      case 'overriddenToHoliday':
        return `${baseClasses} bg-red-200 text-red-900 ring-2 ring-red-400 hover:bg-red-300`;
      default:
        return `${baseClasses} hover:bg-gray-100`;
    }
  };

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Business Day Schedule</h1>
      
      {/* Legend */}
      <div className="mb-6 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-green-100 border border-green-200 rounded"></div>
          <span>Business Day</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-red-100 border border-red-200 rounded"></div>
          <span>Holiday</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-green-200 border border-green-400 border-2 rounded"></div>
          <span>Override → Business</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-red-200 border border-red-400 border-2 rounded"></div>
          <span>Override → Holiday</span>
        </div>
      </div>

      <div className="text-sm text-gray-600 mb-4">
        Double-click any day to toggle an override
      </div>

      {/* Calendar */}
      <div className="border rounded-lg p-4 bg-white">
        {/* Calendar Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <button
              onClick={() => changeYear(-1)}
              className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-sm"
            >
              ««
            </button>
            <button
              onClick={() => changeMonth(-1)}
              className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-sm"
            >
              «
            </button>
          </div>
          
          <h2 className="text-lg font-semibold">
            {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
          </h2>
          
          <div className="flex items-center gap-2">
            <button
              onClick={() => changeMonth(1)}
              className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-sm"
            >
              »
            </button>
            <button
              onClick={() => changeYear(1)}
              className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-sm"
            >
              »»
            </button>
          </div>
        </div>

        {/* Week day headers */}
        <div className="grid grid-cols-7 gap-1 mb-2">
          {weekDays.map(day => (
            <div key={day} className="text-center text-sm font-medium text-gray-600 py-2">
              {day}
            </div>
          ))}
        </div>

        {/* Calendar grid */}
        <div className="grid grid-cols-7 gap-1">
          {generateCalendarDays().map((day, index) => (
            <div key={index} className="flex justify-center">
              {day ? (
                <div
                  className={getDayClasses(getDayState(day))}
                  onDoubleClick={() => handleDayDoubleClick(day)}
                  title={`Double-click to toggle override for ${monthNames[currentDate.getMonth()]} ${day}`}
                >
                  {day}
                </div>
              ) : (
                <div className="w-10 h-10"></div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Current Overrides Display */}
      <div className="mt-6">
        <h2 className="text-lg font-semibold mb-3">Current Overrides</h2>
        {Object.keys(overrides).length === 0 ? (
          <p className="text-gray-500 italic">No overrides set</p>
        ) : (
          <div className="space-y-2">
            {Object.entries(overrides).map(([date, type]) => (
              <div key={date} className="flex items-center gap-3 text-sm">
                <span className="font-mono bg-gray-100 px-2 py-1 rounded">
                  {date}
                </span>
                <span className={`px-2 py-1 rounded text-xs font-medium ${
                  type === 'business' 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                }`}>
                  Override to {type === 'business' ? 'Business Day' : 'Holiday'}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default BusinessDayCalendar;