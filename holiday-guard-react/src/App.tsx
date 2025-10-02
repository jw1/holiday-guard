import React, {useEffect, useState} from 'react';
import {Routes, Route, Navigate, Outlet, useNavigate} from 'react-router-dom';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {ReactQueryDevtools} from '@tanstack/react-query-devtools';
import {AuthProvider, useAuth} from './context/AuthContext';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import Schedules from './components/Schedules';
import ScheduleViewer from './components/ScheduleViewer';
import AuditLog from './components/AuditLog';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import {setOnUnauthorized} from './services/api';

// Create a client
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5, // 5 minutes
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
});

/**
 * Main application layout including sidebar and header.
 * The content is rendered via the <Outlet /> component from react-router-dom.
 */
const AppLayout = () => {
    const [isSidebarOpen, setSidebarOpen] = useState(false);

    return (
        <div className="flex bg-gray-100 h-screen overflow-hidden">
            <Sidebar isOpen={isSidebarOpen} setIsOpen={setSidebarOpen}/>
            <div className="flex-1 flex flex-col">
                <Header onMenuClick={() => setSidebarOpen(true)}/>
                <main className="flex-1 overflow-y-auto">
                    <Outlet/>
                </main>
            </div>
            {/* Backdrop for mobile sidebar */}
            {isSidebarOpen && (
                <div
                    className="lg:hidden fixed inset-0 bg-black opacity-50 z-30"
                    onClick={() => setSidebarOpen(false)}
                ></div>
            )}
        </div>
    );
};

/**
 * Inner component that sets up the 401 interceptor.
 * Must be inside AuthProvider to access auth context.
 */
const AppRoutes = () => {
    const navigate = useNavigate();
    const {logout} = useAuth();

    useEffect(() => {
        // Register the 401 handler to clear auth and redirect to login
        setOnUnauthorized(() => {
            logout();
            navigate('/login', {replace: true});
        });
    }, [navigate, logout]);

    return (
        <Routes>
            <Route path="/login" element={<LoginPage/>}/>
            <Route element={<ProtectedRoute/>}>
                <Route element={<AppLayout/>}>
                    <Route path="/" element={<Navigate to="/dashboard"/>}/>
                    <Route path="/dashboard" element={<Dashboard/>}/>
                    <Route path="/schedules" element={<Schedules/>}/>
                    <Route path="/schedule-viewer" element={<ScheduleViewer/>}/>
                    <Route path="/audit-log" element={<AuditLog/>}/>
                </Route>
            </Route>
        </Routes>
    );
};

function App(): React.ReactElement {
    return (
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <AppRoutes/>
            </AuthProvider>
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    );
}

export default App;
