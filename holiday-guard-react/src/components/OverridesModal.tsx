import { FC, useState, useEffect } from 'react';
import { Schedule } from './Schedules';
import ScheduleOverridesCalendar from './ScheduleOverridesCalendar';

interface OverridesModalProps {
  schedule: Schedule | null;
  onClose: () => void;
  onSave: (overrides: { [date: string]: 'FORCE_RUN' | 'SKIP' }) => void;
}

const OverridesModal: FC<OverridesModalProps> = ({ schedule, onClose, onSave }) => {
  const [overrides, setOverrides] = useState<{ [date: string]: 'FORCE_RUN' | 'SKIP' }>({});
  const [baseCalendar, setBaseCalendar] = useState<{ [date: string]: 'run' | 'no-run' }>({});

  useEffect(() => {
    if (schedule) {
      // Fetch overrides for the schedule
      fetch(`/api/v1/schedules/${schedule.id}/overrides`)
        .then(res => res.json())
        .then(data => {
          const formattedOverrides = data.reduce((acc: any, override: any) => {
            acc[override.date] = override.type;
            return acc;
          }, {});
          setOverrides(formattedOverrides);
        })
        .catch(error => console.error('Error fetching overrides:', error));

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

  const handleOverridesChange = (newOverrides: { [date: string]: 'FORCE_RUN' | 'SKIP' }) => {
    setOverrides(newOverrides);
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
        overrides: Object.entries(overrides).map(([date, type]) => ({
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
          onSave(overrides);
        })
        .catch(error => {
          console.error('Error saving overrides:', error);
          window.alert('Failed to save overrides. Please try again.');
        });
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="w-full max-w-4xl p-8 bg-white rounded-lg shadow-xl">
        <h2 className="mb-4 text-2xl font-bold">Manage Overrides for {schedule.name}</h2>
        
        <ScheduleOverridesCalendar 
          baseCalendar={baseCalendar}
          initialOverrides={overrides}
          onOverridesChange={handleOverridesChange}
        />

        <div className="flex justify-end mt-6">
          <button onClick={onClose} className="px-4 py-2 mr-2 font-bold text-gray-700 bg-gray-200 rounded hover:bg-gray-300">
            Cancel
          </button>
          <button onClick={handleSave} className="px-4 py-2 font-bold text-white bg-blue-500 rounded hover:bg-blue-700">
            Save
          </button>
        </div>
      </div>
    </div>
  );
};

export default OverridesModal;
