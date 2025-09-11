import React from 'react';
import './App.css';
import HealthCheck from './components/HealthCheck'; // Import the new component

function App(): React.ReactElement {
  return (
    <div className="App">
      <header className="App-header">
        <h1 className="text-3xl font-bold text-center">Holiday Guard Admin</h1>
        <HealthCheck /> {/* Render the new component */}
      </header>
    </div>
  );
}

export default App;