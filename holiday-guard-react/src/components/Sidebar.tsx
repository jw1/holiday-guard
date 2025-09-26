import {HomeIcon, CalendarIcon, BookOpenIcon} from '@heroicons/react/24/outline';
import {Link, useLocation} from 'react-router-dom';

const Sidebar = () => {

    const location = useLocation();

    const navItemClasses = (path: string) =>
        `flex items-center p-2 rounded cursor-pointer ${location.pathname.startsWith(path) ? 'bg-gray-900' : 'hover:bg-gray-700'}`;

    return (
        <aside className="bg-gray-800 text-white w-64 min-h-screen p-4 flex-shrink-0">
            <div className="flex items-center mb-10">
                <img src="/favicon3.png" alt="Logo" className="h-8 w-8 mr-2"/>
                <div className="text-2xl font-bold">Holiday Guard</div>
            </div>
            <nav>
                <ul>
                    <li className="mb-2">
                        <Link to="/dashboard" className={navItemClasses('/dashboard')}>
                            <HomeIcon className="h-6 w-6 mr-2"/>
                            Dashboard
                        </Link>
                    </li>
                    <li className="mb-2">
                        <Link to="/schedules" className={navItemClasses('/schedules')}>
                            <CalendarIcon className="h-6 w-6 mr-2"/>
                            Schedules
                        </Link>
                    </li>
                    <li className="mb-2">
                        <Link to="/audit-log" className={navItemClasses('/audit-log')}>
                            <BookOpenIcon className="h-6 w-6 mr-2"/>
                            Audit Log
                        </Link>
                    </li>
                </ul>
            </nav>
        </aside>
    );
};

export default Sidebar;
