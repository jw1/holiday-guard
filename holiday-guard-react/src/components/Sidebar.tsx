import React from 'react';
import {
  HomeIcon,
  UsersIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';

const Sidebar = () => {
  return (
    <aside className="bg-gray-800 text-white w-64 min-h-screen p-4">
      <div className="flex items-center mb-10">
        <img src="/favicon3.png" alt="Logo" className="h-8 w-8 mr-2" />
        <div className="text-2xl font-bold">Holiday Guard</div>
      </div>
      <nav>
        <ul>
          <li className="mb-2">
            <a href="#" className="flex items-center p-2 rounded hover:bg-gray-700">
              <HomeIcon className="h-6 w-6 mr-2" />
              Dashboard
            </a>
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