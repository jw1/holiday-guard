

import { useLocation } from 'react-router-dom';

const Header = () => {
  const location = useLocation();

  // Create a user-friendly title from the pathname
  const getTitle = (pathname: string) => {
    if (pathname.startsWith('/schedules')) return 'Schedules';
    if (pathname.startsWith('/dashboard')) return 'Dashboard';
    return 'Home';
  };

  const title = getTitle(location.pathname);

  return (
    <header className="bg-white shadow-md p-4 flex justify-between items-center">
      <div>
        <h1 className="text-xl text-gray-600">{title}</h1>
        <p className="text-sm text-gray-500">Home / {title}</p>
      </div>
      <div className="flex items-center">
        <div className="mr-4">
          <span>🔔</span>
        </div>
        <div>
          <img src="https://i.pravatar.cc/40" alt="User Avatar" className="rounded-full" />
        </div>
      </div>
    </header>
  );
};

export default Header;
