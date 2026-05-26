import React, { useState } from 'react';
import { toast } from 'react-hot-toast';

const SectionHeader = ({ title, description }) => (
  <div style={{ marginBottom: 20 }}>
    <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B' }}>{title}</h2>
    {description && <p style={{ fontSize: '0.82rem', color: '#64748B', marginTop: 4 }}>{description}</p>}
  </div>
);

const Divider = () => <div style={{ height: 1, background: '#F1F5F9', margin: '28px 0' }} />;

function Settings() {
  const username = localStorage.getItem('username') || 'User';
  const [displayName, setDisplayName] = useState(username);
  const [currency, setCurrency] = useState('INR');
  const [notifications, setNotifications] = useState({ weekly: false, budgetAlerts: true, bills: true });

  const handleSaveProfile = (e) => {
    e.preventDefault();
    toast.success('Profile saved!');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24, maxWidth: 700 }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Settings</h1>
        <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Manage your account preferences.</p>
      </div>

      {/* Profile Section */}
      <div className="card" style={{ padding: 28 }}>
        <SectionHeader title="Profile Information" description="Update your personal details." />

        <div style={{ display: 'flex', alignItems: 'center', gap: 18, marginBottom: 24 }}>
          <div style={{
            width: 72, height: 72, borderRadius: '50%',
            background: 'linear-gradient(135deg,#EDE9FE,#DDD6FE)',
            color: '#7C3AED', fontWeight: 800, fontSize: '1.6rem',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            border: '2px solid #C4B5FD', flexShrink: 0,
          }}>
            {username.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <p style={{ fontWeight: 700, fontSize: '1.05rem', color: '#1E293B' }}>{username}</p>
            <p style={{ fontSize: '0.85rem', color: '#94A3B8', marginTop: 3 }}>{username.toLowerCase()}@gmail.com</p>
          </div>
        </div>

        <form onSubmit={handleSaveProfile} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.78rem', fontWeight: 700, color: '#374151', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Display Name
            </label>
            <input
              type="text"
              value={displayName}
              onChange={e => setDisplayName(e.target.value)}
              className="input"
              style={{ maxWidth: 320 }}
            />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.78rem', fontWeight: 700, color: '#374151', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Email
            </label>
            <input
              type="email"
              value={`${username.toLowerCase()}@gmail.com`}
              disabled
              className="input"
              style={{ maxWidth: 320, opacity: 0.6, cursor: 'not-allowed' }}
            />
          </div>
          <div>
            <button type="submit" className="btn-primary" style={{ marginTop: 4 }}>
              Save Profile
            </button>
          </div>
        </form>

        <Divider />

        {/* Currency */}
        <SectionHeader title="Currency" description="Set your preferred currency for display." />
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <select
            value={currency}
            onChange={e => setCurrency(e.target.value)}
            className="select"
            style={{ maxWidth: 200 }}
          >
            <option value="INR">🇮🇳 INR — ₹ Indian Rupee</option>
            <option value="USD">🇺🇸 USD — $ US Dollar</option>
            <option value="EUR">🇪🇺 EUR — € Euro</option>
            <option value="GBP">🇬🇧 GBP — £ British Pound</option>
          </select>
          <button className="btn-secondary" onClick={() => toast.success(`Currency set to ${currency}`)}>
            Apply
          </button>
        </div>

        <Divider />

        {/* Notifications */}
        <SectionHeader title="Notifications" description="Choose what updates you want to receive." />
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {[
            { key: 'weekly',       label: 'Weekly Summary', desc: 'Receive a weekly spending report' },
            { key: 'budgetAlerts', label: 'Budget Alerts',  desc: 'Get notified when nearing budget limits' },
            { key: 'bills',        label: 'Bill Reminders', desc: 'Reminders 3 days before bills are due' },
          ].map(item => (
            <div key={item.key} style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '14px 16px', borderRadius: 10, border: '1px solid #E2E8F0',
              background: '#F8F7FF',
            }}>
              <div>
                <p style={{ fontWeight: 600, fontSize: '0.875rem', color: '#1E293B' }}>{item.label}</p>
                <p style={{ fontSize: '0.78rem', color: '#94A3B8', marginTop: 2 }}>{item.desc}</p>
              </div>
              {/* Toggle */}
              <button
                onClick={() => setNotifications(n => ({ ...n, [item.key]: !n[item.key] }))}
                style={{
                  width: 44, height: 24, borderRadius: 12, border: 'none', cursor: 'pointer',
                  background: notifications[item.key] ? '#7C3AED' : '#CBD5E1',
                  position: 'relative', transition: 'background 0.2s', flexShrink: 0,
                }}
                aria-label={`Toggle ${item.label}`}
              >
                <div style={{
                  position: 'absolute', top: 3, width: 18, height: 18, borderRadius: '50%',
                  background: '#fff', boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
                  transition: 'left 0.2s',
                  left: notifications[item.key] ? 23 : 3,
                }} />
              </button>
            </div>
          ))}
        </div>

        <Divider />

        {/* Danger zone */}
        <SectionHeader title="Danger Zone" description="Irreversible actions — proceed with caution." />
        <button
          onClick={() => toast.error('Account deletion is disabled in demo mode.')}
          style={{
            background: 'none', border: '1px solid #EF4444', color: '#EF4444',
            borderRadius: 8, padding: '9px 20px', cursor: 'pointer',
            fontWeight: 600, fontSize: '0.875rem', transition: 'background 0.15s',
          }}
          onMouseEnter={e => { e.currentTarget.style.background = '#FEF2F2'; }}
          onMouseLeave={e => { e.currentTarget.style.background = 'none'; }}
        >
          Delete Account
        </button>
      </div>
    </div>
  );
}

export default Settings;
