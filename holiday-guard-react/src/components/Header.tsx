

const Header = () => {
  return (
    <header className="bg-white shadow-md p-4 flex justify-between items-center">
      <div>
        <h1 className="text-xl text-gray-600">Dashboard</h1>
        <p className="text-sm text-gray-500">Home / Dashboard</p>
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
