import React from 'react';
import { Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import Schedules from './components/Schedules';
import AuditLog from './components/AuditLog';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';

/**
 * Main application layout including sidebar and header.
 * The content is rendered via the <Outlet /> component from react-router-dom.
 */
const AppLayout = () => (
  <div className="flex bg-gray-100 min-h-screen">
    <Sidebar />
    <div className="flex-1 flex flex-col">
      <Header />
      <Outlet />
    </div>
  </div>
);

function App(): React.ReactElement {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<Navigate to="/dashboard" />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/schedules" element={<Schedules />} />
            <Route path="/audit-log" element={<AuditLog />} />
          </Route>
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;
