import React from 'react';

const TailwindTestPage: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-blue-500">
      <div className="bg-white p-8 rounded-lg shadow-lg text-center">
        <h1 className="text-4xl font-bold text-purple-700 mb-4">Tailwind CSS Test Page</h1>
        <p className="text-lg text-gray-700 mb-6">
          If you see this styled, Tailwind CSS is working!
        </p>
        <button className="px-6 py-3 bg-green-500 text-white rounded-full hover:bg-green-600 transition duration-300">
          Awesome Button
        </button>
      </div>
    </div>
  );
};

export default TailwindTestPage;
