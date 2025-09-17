import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import Schedules from './components/Schedules';

export type Page = 'dashboard' | 'schedules';

function App(): React.ReactElement {
  const [page, setPage] = useState<Page>('dashboard');

  return (
    <div className="flex bg-gray-100 min-h-screen">
      <Sidebar page={page} setPage={setPage} />
      <div className="flex-1 flex flex-col">
        <Header />
        {page === 'dashboard' && <Dashboard />}
        {page === 'schedules' && <Schedules />}
      </div>
    </div>
  );
}

export default App;
