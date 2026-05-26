import React, { useState, useEffect } from 'react';
import { getExpenseSummary } from '../api/expenseApi';
import { toast } from 'react-hot-toast';
import Spinner from '../components/Spinner';

const CATEGORY_META = {
  Food:          { icon: '🍔', color: '#EF4444', bg: '#FEE2E2' },
  Travel:        { icon: '✈️', color: '#3B82F6', bg: '#DBEAFE' },
  Shopping:      { icon: '🛍️', color: '#8B5CF6', bg: '#EDE9FE' },
  Bills:         { icon: '⚡', color: '#F59E0B', bg: '#FEF3C7' },
  Entertainment: { icon: '🎬', color: '#EC4899', bg: '#FCE7F3' },
  Health:        { icon: '💊', color: '#10B981', bg: '#D1FAE5' },
  Transport:     { icon: '🚗', color: '#06B6D4', bg: '#CFFAFE' },
  Education:     { icon: '📚', color: '#7C3AED', bg: '#EDE9FE' },
  Other:         { icon: '📦', color: '#64748B', bg: '#F1F5F9' },
};

function Categories() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getExpenseSummary()
      .then(r => setSummary(r.data || {}))
      .catch(() => toast.error('Failed to load categories.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', flexDirection: 'column', gap: 12 }}>
        <Spinner size="lg" /><p style={{ color: '#64748B', fontWeight: 500 }}>Loading categories...</p>
      </div>
    );
  }

  const categoryTotals = summary?.categoryTotals || [];
  // Build a complete list — categories with data + empty ones from CATEGORY_META
  const allCategories = Object.keys(CATEGORY_META).map(name => {
    const found = categoryTotals.find(c => c.category === name);
    return { name, total: found?.total || 0, count: found?.count || 0 };
  });
  const totalSpent = allCategories.reduce((s, c) => s + c.total, 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Categories</h1>
          <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Spending breakdown by category.</p>
        </div>
        <button
          className="btn-primary"
          onClick={() => toast('Custom categories coming soon!')}
        >
          <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Add Category
        </button>
      </div>

      {/* Category Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16 }}>
        {allCategories.map(cat => {
          const meta = CATEGORY_META[cat.name] || { icon: '📦', color: '#64748B', bg: '#F1F5F9' };
          const pct = totalSpent > 0 ? ((cat.total / totalSpent) * 100).toFixed(1) : 0;
          return (
            <div
              key={cat.name}
              className="card"
              style={{
                display: 'flex', flexDirection: 'column', gap: 14,
                transition: 'box-shadow 0.2s, transform 0.2s',
                cursor: 'default',
              }}
              onMouseEnter={e => { e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.1)'; e.currentTarget.style.transform = 'translateY(-2px)'; }}
              onMouseLeave={e => { e.currentTarget.style.boxShadow = ''; e.currentTarget.style.transform = ''; }}
            >
              <div style={{
                width: 48, height: 48, borderRadius: 12,
                background: meta.bg, fontSize: 22,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>{meta.icon}</div>

              <div>
                <p style={{ fontWeight: 700, fontSize: '0.95rem', color: '#1E293B' }}>{cat.name}</p>
                <p style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: 2 }}>{cat.count} transactions</p>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                  <span style={{ fontSize: '1rem', fontWeight: 800, color: '#1E293B' }}>
                    ₹{cat.total.toLocaleString('en-IN')}
                  </span>
                  <span style={{ fontSize: '0.72rem', fontWeight: 600, color: meta.color }}>
                    {pct}%
                  </span>
                </div>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{ width: `${Math.min(100, pct)}%`, background: meta.color }}
                  />
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Categories;
