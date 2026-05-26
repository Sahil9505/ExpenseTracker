import React from 'react';

const BILLS_DATA = [
  { name: 'Electricity Bill', icon: '⚡', dueDate: '2026-05-25', amount: 1200, status: 'Due Soon',  color: '#F59E0B', category: 'Utilities' },
  { name: 'Internet Bill',    icon: '🌐', dueDate: '2026-05-28', amount:  799, status: 'Due Soon',  color: '#3B82F6', category: 'Utilities' },
  { name: 'House Rent',       icon: '🏠', dueDate: '2026-06-01', amount: 8500, status: 'Upcoming',  color: '#7C3AED', category: 'Housing'   },
  { name: 'Water Bill',       icon: '💧', dueDate: '2026-06-05', amount:  350, status: 'Upcoming',  color: '#06B6D4', category: 'Utilities' },
  { name: 'Mobile Recharge',  icon: '📱', dueDate: '2026-05-20', amount:  299, status: 'Paid',      color: '#10B981', category: 'Telecom'   },
  { name: 'Credit Card',      icon: '💳', dueDate: '2026-06-10', amount: 4500, status: 'Upcoming',  color: '#EF4444', category: 'Finance'   },
];

const statusMeta = {
  'Due Soon': { bg: '#FEF3C7', color: '#92400E' },
  'Upcoming':  { bg: '#DBEAFE', color: '#1E40AF' },
  'Paid':      { bg: '#D1FAE5', color: '#065F46' },
};

function Bills() {
  const total = BILLS_DATA.filter(b => b.status !== 'Paid').reduce((s, b) => s + b.amount, 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Bills & Reminders</h1>
          <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Never miss a due date again.</p>
        </div>
        <button className="btn-primary" onClick={() => alert('Add bill coming soon!')}>
          <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Add Bill
        </button>
      </div>

      {/* Summary strip */}
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
        {[
          { label: 'Total Pending', value: `₹${total.toLocaleString('en-IN')}`, bg: '#EDE9FE', color: '#7C3AED' },
          { label: 'Due Soon', value: BILLS_DATA.filter(b => b.status === 'Due Soon').length, bg: '#FEF3C7', color: '#92400E' },
          { label: 'Paid', value: BILLS_DATA.filter(b => b.status === 'Paid').length, bg: '#D1FAE5', color: '#065F46' },
        ].map(chip => (
          <div key={chip.label} style={{
            background: chip.bg, color: chip.color, borderRadius: 10, padding: '10px 18px',
            display: 'flex', flexDirection: 'column', gap: 2,
          }}>
            <span style={{ fontSize: '1.2rem', fontWeight: 800 }}>{chip.value}</span>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, opacity: 0.8 }}>{chip.label}</span>
          </div>
        ))}
      </div>

      {/* Bills table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Bill</th>
                <th>Category</th>
                <th>Due Date</th>
                <th style={{ textAlign: 'right' }}>Amount</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th style={{ textAlign: 'center' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {BILLS_DATA.map(bill => {
                const meta = statusMeta[bill.status] || statusMeta['Upcoming'];
                const daysLeft = Math.ceil((new Date(bill.dueDate) - new Date()) / 86400000);
                return (
                  <tr key={bill.name}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <div style={{
                          width: 38, height: 38, borderRadius: 10, flexShrink: 0,
                          background: bill.color + '18', fontSize: 18,
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                        }}>{bill.icon}</div>
                        <div>
                          <p style={{ fontWeight: 600, fontSize: '0.9rem', color: '#1E293B' }}>{bill.name}</p>
                          {bill.status !== 'Paid' && (
                            <p style={{ fontSize: '0.72rem', color: daysLeft <= 5 ? '#EF4444' : '#94A3B8', marginTop: 2, fontWeight: 500 }}>
                              {daysLeft > 0 ? `${daysLeft} days left` : 'Overdue'}
                            </p>
                          )}
                        </div>
                      </div>
                    </td>
                    <td><span style={{ fontSize: '0.8rem', color: '#64748B', fontWeight: 500 }}>{bill.category}</span></td>
                    <td style={{ fontSize: '0.85rem', color: '#64748B', fontWeight: 500, whiteSpace: 'nowrap' }}>
                      {new Date(bill.dueDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 700, fontSize: '0.9rem', color: '#1E293B' }}>
                      ₹{bill.amount.toLocaleString('en-IN')}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span style={{
                        padding: '3px 12px', borderRadius: 99, fontSize: '0.72rem', fontWeight: 700,
                        background: meta.bg, color: meta.color,
                      }}>{bill.status}</span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      {bill.status !== 'Paid' ? (
                        <button style={{
                          background: '#7C3AED', color: '#fff', border: 'none', borderRadius: 6,
                          padding: '6px 14px', cursor: 'pointer', fontSize: '0.78rem', fontWeight: 700,
                          transition: 'background 0.15s',
                        }}
                          onMouseEnter={e => e.currentTarget.style.background = '#6D28D9'}
                          onMouseLeave={e => e.currentTarget.style.background = '#7C3AED'}
                        >Pay Now</button>
                      ) : (
                        <span style={{ fontSize: '0.8rem', color: '#94A3B8', fontWeight: 500 }}>✓ Done</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Bills;
