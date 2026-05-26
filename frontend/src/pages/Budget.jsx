import React, { useState, useEffect } from 'react';
import { getExpenseSummary } from '../api/expenseApi';
import { toast } from 'react-hot-toast';
import Spinner from '../components/Spinner';

const BUDGETS = {
  Food:          { limit: 8000,  icon: '🍔' },
  Travel:        { limit: 5000,  icon: '✈️' },
  Shopping:      { limit: 6000,  icon: '🛍️' },
  Bills:         { limit: 4000,  icon: '⚡' },
  Entertainment: { limit: 3000,  icon: '🎬' },
  Health:        { limit: 2500,  icon: '💊' },
  Transport:     { limit: 3500,  icon: '🚗' },
  Education:     { limit: 5000,  icon: '📚' },
  Other:         { limit: 2000,  icon: '📦' },
};

function getBarColor(pct) {
  if (pct >= 90) return '#EF4444';
  if (pct >= 70) return '#F59E0B';
  return '#10B981';
}
function getBadge(pct) {
  if (pct >= 90) return { label: 'Over budget', bg: '#FEE2E2', color: '#991B1B' };
  if (pct >= 70) return { label: 'Near limit', bg: '#FEF3C7', color: '#92400E' };
  return { label: 'On track', bg: '#D1FAE5', color: '#065F46' };
}

function Budget() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getExpenseSummary()
      .then(r => setSummary(r.data || {}))
      .catch(() => toast.error('Failed to load budget data.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', flexDirection: 'column', gap: 12 }}>
        <Spinner size="lg" /><p style={{ color: '#64748B', fontWeight: 500 }}>Loading budgets...</p>
      </div>
    );
  }

  const categoryTotals = summary?.categoryTotals || [];
  const totalMonthlyBudget = Object.values(BUDGETS).reduce((s, b) => s + b.limit, 0);
  const totalSpent = categoryTotals.reduce((s, c) => s + c.total, 0);
  const totalPct = Math.min(100, ((totalSpent / totalMonthlyBudget) * 100).toFixed(1));

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Budget</h1>
        <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Track your monthly spending limits.</p>
      </div>

      {/* Total overview card */}
      <div className="card" style={{ padding: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
          <div>
            <p style={{ fontSize: '0.8rem', color: '#64748B', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Total Monthly Budget</p>
            <p style={{ fontSize: '1.8rem', fontWeight: 800, color: '#1E293B', marginTop: 4 }}>
              ₹{totalSpent.toLocaleString('en-IN')}
              <span style={{ fontSize: '1rem', color: '#94A3B8', fontWeight: 500 }}> / ₹{totalMonthlyBudget.toLocaleString('en-IN')}</span>
            </p>
          </div>
          <div style={{
            fontSize: '2rem', fontWeight: 800,
            color: getBarColor(totalPct),
          }}>{totalPct}%</div>
        </div>
        <div className="progress-bar" style={{ height: 12 }}>
          <div className="progress-fill" style={{ width: `${totalPct}%`, background: getBarColor(totalPct) }} />
        </div>
        <p style={{ fontSize: '0.8rem', color: '#64748B', marginTop: 10 }}>
          Remaining: <strong style={{ color: '#1E293B' }}>₹{Math.max(0, totalMonthlyBudget - totalSpent).toLocaleString('en-IN')}</strong>
        </p>
      </div>

      {/* Per-category budget bars */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 16 }}>
        {Object.entries(BUDGETS).map(([name, budget]) => {
          const spent = categoryTotals.find(c => c.category === name)?.total || 0;
          const pct = Math.min(100, parseFloat(((spent / budget.limit) * 100).toFixed(1)));
          const barColor = getBarColor(pct);
          const badge = getBadge(pct);
          return (
            <div key={name} className="card" style={{ padding: 20 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <span style={{ fontSize: 20 }}>{budget.icon}</span>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#1E293B' }}>{name}</span>
                </div>
                <span style={{
                  fontSize: '0.7rem', fontWeight: 700, padding: '3px 10px', borderRadius: 99,
                  background: badge.bg, color: badge.color,
                }}>{badge.label}</span>
              </div>

              <div className="progress-bar" style={{ marginBottom: 10 }}>
                <div className="progress-fill" style={{ width: `${pct}%`, background: barColor }} />
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', fontWeight: 600 }}>
                <span style={{ color: barColor }}>₹{spent.toLocaleString('en-IN')} spent</span>
                <span style={{ color: '#94A3B8' }}>₹{budget.limit.toLocaleString('en-IN')} limit</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Budget;
