import React, { useState, useEffect } from 'react';
import { Schedule } from './Schedules';

interface ScheduleModalProps {
  schedule: Schedule | null;
  onClose: () => void;
  onSave: (scheduleData: Omit<Schedule, 'id' | 'createdDate'>) => void;
}

const ScheduleModal = ({ schedule, onClose, onSave }: ScheduleModalProps) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [country, setCountry] = useState('US');
  const [isActive, setIsActive] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (schedule) {
      setName(schedule.name);
      setDescription(schedule.description);
      setCountry(schedule.country);
      setIsActive(schedule.status === 'Active');
    } else {
      // Reset form for new schedule
      setName('');
      setDescription('');
      setCountry('US');
      setIsActive(true);
    }
    setError(''); // Clear errors when modal opens or schedule changes
  }, [schedule]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Name is a required field.');
      return;
    }
    onSave({
      name,
      description,
      country,
      status: isActive ? 'Active' : 'Inactive',
    });
  };

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
        <h2 className="text-2xl font-bold mb-4">{schedule ? 'Edit Schedule' : 'New Schedule'}</h2>
        {error && <p className="text-red-500 text-xs italic mb-4">{error}</p>}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="name" className="block text-gray-700 text-sm font-bold mb-2">Name</label>
            <input 
              type="text" 
              id="name" 
              name="name"
              value={name} 
              onChange={(e) => setName(e.target.value)} 
              autoComplete="off"
              className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${error ? 'border-red-500' : ''}`} 
            />
          </div>
          <div className="mb-4">
            <label htmlFor="description" className="block text-gray-700 text-sm font-bold mb-2">Description</label>
            <textarea 
              id="description" 
              rows={3} 
              value={description} 
              onChange={(e) => setDescription(e.target.value)} 
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            ></textarea>
          </div>
          <div className="mb-4">
            <label htmlFor="country" className="block text-gray-700 text-sm font-bold mb-2">Country</label>
            <select 
              id="country" 
              value={country} 
              onChange={(e) => setCountry(e.target.value)} 
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            >
              <option>US</option>
              <option>UK</option>
              <option>CA</option>
              <option>AU</option>
            </select>
          </div>
          <div className="mb-6">
            <label className="flex items-center">
              <input 
                type="checkbox" 
                checked={isActive} 
                onChange={(e) => setIsActive(e.target.checked)} 
                className="form-checkbox h-5 w-5 text-blue-600" 
              />
              <span className="ml-2 text-sm text-gray-700">Active</span>
            </label>
          </div>
          <div className="flex items-center justify-end">
            <button type="button" onClick={onClose} className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded mr-2">
              Cancel
            </button>
            <button type="submit" className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ScheduleModal;