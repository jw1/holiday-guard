import {useLocation} from 'react-router-dom';
import {UserMenu} from "./UserMenu";
import {Bars3Icon} from "@heroicons/react/24/outline";

interface HeaderProps {
    onMenuClick: () => void;
}

const Header = ({ onMenuClick }: HeaderProps) => {

    const location = useLocation();

    /**
     * create title + breadcrumb text from URL
     *
     * @param pathname path given by router
     */
    const getTitle = (pathname: string) => {
        if (pathname.startsWith('/schedule-viewer')) return 'Schedule Viewer';
        if (pathname.startsWith('/schedules')) return 'Schedules';
        if (pathname.startsWith('/dashboard')) return 'Dashboard';
        if (pathname.startsWith('/audit-log')) return 'Audit Log';
        return '';
    };

    const title = getTitle(location.pathname);

    return (
        <header className="bg-white shadow-md p-4 flex justify-between items-center z-20">
            <div className="flex items-center">
                <button onClick={onMenuClick} className="lg:hidden text-gray-500 hover:text-gray-700 mr-4">
                    <Bars3Icon className="h-6 w-6"/>
                </button>
                <div>
                    <h1 className="text-xl px-6 text-gray-600">{title}</h1>
                    <p className="text-sm px-6 text-gray-500">Home / {title}</p>
                </div>
            </div>
            <div className="flex items-center">
                <div>
                    <UserMenu/>
                </div>
            </div>
        </header>
    );
};

export default Header;
