import React, {useEffect} from 'react';
import {Routes, Route, Navigate, Outlet, useNavigate} from 'react-router-dom';
import {AuthProvider, useAuth} from './context/AuthContext';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import Schedules from './components/Schedules';
import AuditLog from './components/AuditLog';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import {setOnUnauthorized} from './services/api';

/**
 * Main application layout including sidebar and header.
 * The content is rendered via the <Outlet /> component from react-router-dom.
 */
const AppLayout = () => (
    <div className="flex bg-gray-100 min-h-screen">
        <Sidebar/>
        <div className="flex-1 flex flex-col">
            <Header/>
            <Outlet/>
        </div>
    </div>
);

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
                    <Route path="/audit-log" element={<AuditLog/>}/>
                </Route>
            </Route>
        </Routes>
    );
};

function App(): React.ReactElement {
    return (
        <AuthProvider>
            <AppRoutes/>
        </AuthProvider>
    );
}

export default App;
