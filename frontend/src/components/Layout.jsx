import React, { useState } from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

const Layout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <>
      {/* Mobile overlay */}
      <div
        className={`sidebar-overlay ${sidebarOpen ? 'show' : ''}`}
        onClick={() => setSidebarOpen(false)}
      />

      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* Main shell: pushed right by sidebar width on desktop, full width on mobile */}
      <div className="main-shell">
        <Navbar onMenuClick={() => setSidebarOpen(true)} />
        <main className="page-content fade-in">
          {children}
        </main>
      </div>
    </>
  );
};

export default Layout;
