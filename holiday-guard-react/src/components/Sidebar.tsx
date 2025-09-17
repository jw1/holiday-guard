import React from 'react';
import {
  HomeIcon,
  UsersIcon,
  ChartBarIcon,
  Cog6ToothIcon,
  CalendarIcon,
} from '@heroicons/react/24/outline';
import { Page } from '../App';

interface SidebarProps {
  page: Page;
  setPage: (page: Page) => void;
}

const Sidebar = ({ page, setPage }: SidebarProps) => {
  const navItemClasses = (isCurrentPage: boolean) =>
    `flex items-center p-2 rounded cursor-pointer ${isCurrentPage ? 'bg-gray-900' : 'hover:bg-gray-700'}`;

  return (
    <aside className="bg-gray-800 text-white w-64 min-h-screen p-4">
      <div className="flex items-center mb-10">
        <img src="/favicon3.png" alt="Logo" className="h-8 w-8 mr-2" />
        <div className="text-2xl font-bold">Holiday Guard</div>
      </div>
      <nav>
        <ul>
          <li className="mb-2">
            <div onClick={() => setPage('dashboard')} className={navItemClasses(page === 'dashboard')}>
              <HomeIcon className="h-6 w-6 mr-2" />
              Dashboard
            </div>
          </li>
          <li className="mb-2">
            <div onClick={() => setPage('schedules')} className={navItemClasses(page === 'schedules')}>
              <CalendarIcon className="h-6 w-6 mr-2" />
              Schedules
            </div>
          </li>
          <li className="mb-2">
            <a href="#" className="flex items-center p-2 rounded hover:bg-gray-700">
              <UsersIcon className="h-6 w-6 mr-2" />
              Users
            </a>
          </li>
          <li className="mb-2">
            <a href="#" className="flex items-center p-2 rounded hover:bg-gray-700">
              <ChartBarIcon className="h-6 w-6 mr-2" />
              Analytics
            </a>
          </li>
          <li className="mb-2">
            <a href="#" className="flex items-center p-2 rounded hover:bg-gray-700">
              <Cog6ToothIcon className="h-6 w-6 mr-2" />
              Settings
            </a>
          </li>
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;
