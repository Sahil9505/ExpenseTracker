import React from 'react';

const RECURRING_DATA = [
  { name: 'Netflix Subscription', amount: 649, frequency: 'Monthly', nextDue: '2026-06-01', status: 'Active', icon: '🎬', color: '#EF4444' },
  { name: 'Gym Membership',       amount: 1200, frequency: 'Monthly', nextDue: '2026-06-05', status: 'Active', icon: '💪', color: '#10B981' },
  { name: 'Spotify Premium',      amount: 119, frequency: 'Monthly', nextDue: '2026-06-10', status: 'Paused', icon: '🎵', color: '#1DB954' },
  { name: 'Amazon Prime',         amount: 299, frequency: 'Monthly', nextDue: '2026-06-15', status: 'Active', icon: '📦', color: '#F59E0B' },
  { name: 'Cloud Storage',        amount: 130, frequency: 'Monthly', nextDue: '2026-06-20', status: 'Active', icon: '☁️', color: '#3B82F6' },
];

function Recurring() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Recurring Expenses</h1>
          <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Manage subscriptions and auto-renewing payments.</p>
        </div>
        <button
          className="btn-primary"
          onClick={() => alert('Add recurring coming soon!')}
        >
          <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Add Recurring
        </button>
      </div>

      {/* Summary chips */}
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
        {[
          { label: 'Total Monthly', value: `₹${RECURRING_DATA.filter(r => r.status === 'Active').reduce((s, r) => s + r.amount, 0).toLocaleString('en-IN')}`, color: '#7C3AED', bg: '#EDE9FE' },
          { label: 'Active', value: RECURRING_DATA.filter(r => r.status === 'Active').length, color: '#10B981', bg: '#D1FAE5' },
          { label: 'Paused', value: RECURRING_DATA.filter(r => r.status === 'Paused').length, color: '#F59E0B', bg: '#FEF3C7' },
        ].map(chip => (
          <div key={chip.label} style={{
            background: chip.bg, color: chip.color,
            borderRadius: 10, padding: '10px 18px',
            display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: 2,
          }}>
            <span style={{ fontSize: '1.2rem', fontWeight: 800 }}>{chip.value}</span>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, opacity: 0.8 }}>{chip.label}</span>
          </div>
        ))}
      </div>

      {/* Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Subscription</th>
                <th>Frequency</th>
                <th>Next Due</th>
                <th style={{ textAlign: 'right' }}>Amount</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {RECURRING_DATA.map(item => (
                <tr key={item.name}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <div style={{
                        width: 38, height: 38, borderRadius: 10, flexShrink: 0,
                        background: item.color + '18', fontSize: 18,
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                      }}>{item.icon}</div>
                      <span style={{ fontWeight: 600, fontSize: '0.9rem', color: '#1E293B' }}>{item.name}</span>
                    </div>
                  </td>
                  <td style={{ fontSize: '0.85rem', color: '#64748B', fontWeight: 500 }}>{item.frequency}</td>
                  <td style={{ fontSize: '0.85rem', color: '#64748B', fontWeight: 500 }}>
                    {new Date(item.nextDue).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                  </td>
                  <td style={{ textAlign: 'right', fontWeight: 700, fontSize: '0.9rem', color: '#1E293B' }}>
                    ₹{item.amount.toLocaleString('en-IN')}
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <span style={{
                      padding: '3px 12px', borderRadius: 99, fontSize: '0.72rem', fontWeight: 700,
                      background: item.status === 'Active' ? '#D1FAE5' : '#FEF3C7',
                      color: item.status === 'Active' ? '#065F46' : '#92400E',
                    }}>{item.status}</span>
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <button style={{
                      background: 'none', border: '1px solid #E2E8F0', borderRadius: 6,
                      padding: '5px 12px', cursor: 'pointer', fontSize: '0.8rem',
                      color: '#64748B', fontWeight: 600, transition: 'background 0.15s',
                    }}
                      onMouseEnter={e => e.currentTarget.style.background = '#F1F5F9'}
                      onMouseLeave={e => e.currentTarget.style.background = 'none'}
                    >
                      {item.status === 'Active' ? 'Pause' : 'Resume'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Recurring;
