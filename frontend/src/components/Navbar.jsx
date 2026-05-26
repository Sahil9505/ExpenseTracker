import React from 'react';
import { useLocation } from 'react-router-dom';

const pageNames = {
  '/dashboard': 'Dashboard',
  '/expenses': 'All Expenses',
  '/add-expense': 'Add Expense',
  '/categories': 'Categories',
  '/reports': 'Reports',
  '/budget': 'Budget',
  '/recurring': 'Recurring',
  '/bills': 'Bills & Reminders',
  '/settings': 'Settings',
};

function Navbar({ onMenuClick }) {
  const location = useLocation();
  const username = localStorage.getItem('username') || 'User';
  const pageName = pageNames[location.pathname] || 'ExpenseTrack';

  return (
    <header className="navbar">
      {/* Left */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        {/* Hamburger (mobile only) */}
        <button
          onClick={onMenuClick}
          aria-label="Open sidebar"
          style={{
            display: 'none',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            color: '#64748B',
            padding: 6,
            borderRadius: 8,
          }}
          className="hamburger-btn"
        >
          <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <div>
          <h2 style={{ fontWeight: 700, fontSize: '1.1rem', color: '#1E293B', letterSpacing: '-0.01em' }}>
            {pageName}
          </h2>
        </div>
      </div>

      {/* Right */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {/* Search (hidden on small) */}
        <div style={{ position: 'relative' }} className="navbar-search">
          <svg
            width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"
            style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: '#94A3B8', pointerEvents: 'none' }}
          >
            <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            type="text"
            placeholder="Search..."
            style={{
              paddingLeft: 38, paddingRight: 14, height: 38, width: 220,
              border: '1px solid #E2E8F0', borderRadius: 8, fontSize: '0.875rem',
              background: '#F8F7FF', outline: 'none', fontFamily: 'inherit',
              color: '#1E293B',
            }}
            onFocus={e => { e.target.style.borderColor = '#7C3AED'; e.target.style.boxShadow = '0 0 0 3px rgba(124,58,237,0.12)'; }}
            onBlur={e => { e.target.style.borderColor = '#E2E8F0'; e.target.style.boxShadow = 'none'; }}
          />
        </div>

        {/* Notification Bell */}
        <button
          style={{ position: 'relative', background: 'none', border: 'none', cursor: 'pointer', color: '#64748B', padding: 8, borderRadius: 8 }}
          onMouseEnter={e => e.currentTarget.style.background = '#F1F5F9'}
          onMouseLeave={e => e.currentTarget.style.background = 'none'}
          aria-label="Notifications"
        >
          <svg width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0" />
          </svg>
          <span style={{
            position: 'absolute', top: 6, right: 6, width: 8, height: 8,
            background: '#EF4444', borderRadius: '50%', border: '2px solid #fff',
          }} />
        </button>

        {/* Avatar */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer' }}>
          <div style={{
            width: 36, height: 36, borderRadius: '50%',
            background: 'linear-gradient(135deg,#EDE9FE,#DDD6FE)',
            color: '#7C3AED', fontWeight: 700, fontSize: '0.85rem',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            border: '1.5px solid #C4B5FD',
          }}>
            {username.slice(0, 2).toUpperCase()}
          </div>
          <span style={{ fontWeight: 600, fontSize: '0.875rem', color: '#374151' }} className="navbar-username">
            {username}
          </span>
        </div>
      </div>
    </header>
  );
}

export default Navbar;
