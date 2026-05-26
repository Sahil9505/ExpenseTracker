import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import {
  ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  LineChart, Line,
} from 'recharts';
import { getExpenseSummary } from '../api/expenseApi';
import Spinner from '../components/Spinner';

const COLORS = ['#7C3AED', '#10B981', '#F59E0B', '#3B82F6', '#EF4444', '#06B6D4', '#EC4899', '#8B5CF6'];

const MONTHLY_DATA = [
  { name: 'Jan', value: 4200 }, { name: 'Feb', value: 3100 }, { name: 'Mar', value: 5400 },
  { name: 'Apr', value: 2780 }, { name: 'May', value: 3890 }, { name: 'Jun', value: 2390 },
  { name: 'Jul', value: 3490 }, { name: 'Aug', value: 4100 }, { name: 'Sep', value: 3700 },
  { name: 'Oct', value: 2900 }, { name: 'Nov', value: 3200 }, { name: 'Dec', value: 2600 },
];

const TABS = ['Category Breakdown', 'Monthly', 'Trends'];

function Reports() {
  const [summary, setSummary] = useState({ totalAmount: 0, categoryTotals: [] });
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState(0);

  useEffect(() => {
    getExpenseSummary()
      .then(r => setSummary(r.data || { totalAmount: 0, categoryTotals: [] }))
      .catch(() => toast.error('Failed to load reports.'))
      .finally(() => setLoading(false));
  }, []);

  const chartData = (summary.categoryTotals || []).map(item => ({ name: item.category, value: item.total }));

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', flexDirection: 'column', gap: 12 }}>
        <Spinner size="lg" /><p style={{ color: '#64748B', fontWeight: 500 }}>Generating reports...</p>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Financial Reports</h1>
        <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Deep dive into your spending habits.</p>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, background: '#F1F5F9', padding: 4, borderRadius: 10, width: 'fit-content' }}>
        {TABS.map((tab, i) => (
          <button
            key={tab}
            onClick={() => setActiveTab(i)}
            style={{
              padding: '8px 18px', borderRadius: 8, border: 'none', cursor: 'pointer',
              fontWeight: 600, fontSize: '0.875rem',
              background: activeTab === i ? '#fff' : 'transparent',
              color: activeTab === i ? '#7C3AED' : '#64748B',
              boxShadow: activeTab === i ? '0 1px 4px rgba(0,0,0,0.08)' : 'none',
              transition: 'all 0.15s',
            }}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Tab: Category Breakdown */}
      {activeTab === 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 20 }}>
            {/* Pie */}
            <div className="card" style={{ padding: 24 }}>
              <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B', marginBottom: 20 }}>Distribution by Category</h2>
              <div style={{ height: 280 }}>
                {chartData.length === 0 ? (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#94A3B8' }}>No data yet</div>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={chartData} cx="50%" cy="50%" innerRadius={75} outerRadius={105} paddingAngle={3} dataKey="value" stroke="none">
                        {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip formatter={v => [`₹${v.toLocaleString('en-IN')}`, '']}
                        contentStyle={{ borderRadius: 10, border: '1px solid #E2E8F0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>

            {/* Bar */}
            <div className="card" style={{ padding: 24 }}>
              <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B', marginBottom: 20 }}>Spending by Category</h2>
              <div style={{ height: 280 }}>
                {chartData.length === 0 ? (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#94A3B8' }}>No data yet</div>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={chartData} margin={{ top: 5, right: 5, left: -20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F1F5F9" />
                      <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#94A3B8' }} />
                      <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#94A3B8' }} tickFormatter={v => `₹${v / 1000}k`} />
                      <Tooltip formatter={v => [`₹${v.toLocaleString('en-IN')}`, 'Amount']}
                        contentStyle={{ borderRadius: 10, border: '1px solid #E2E8F0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                        cursor={{ fill: '#F8F7FF' }} />
                      <Bar dataKey="value" radius={[6, 6, 0, 0]} barSize={28}>
                        {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>
          </div>

          {/* Summary table */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '16px 20px', borderBottom: '1px solid #F1F5F9' }}>
              <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B' }}>Category Summary</h2>
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table className="data-table">
                <thead>
                  <tr><th>Category</th><th style={{ textAlign: 'right' }}>Total Spent</th><th style={{ textAlign: 'right' }}>% of Total</th></tr>
                </thead>
                <tbody>
                  {chartData.length === 0 ? (
                    <tr><td colSpan={3} style={{ textAlign: 'center', padding: 32, color: '#94A3B8' }}>No data available</td></tr>
                  ) : (
                    [...chartData].sort((a, b) => b.value - a.value).map((item, i) => {
                      const pct = summary.totalAmount ? ((item.value / summary.totalAmount) * 100).toFixed(1) : 0;
                      return (
                        <tr key={item.name}>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                              <div style={{ width: 10, height: 10, borderRadius: '50%', background: COLORS[i % COLORS.length] }} />
                              <span style={{ fontWeight: 600 }}>{item.name}</span>
                            </div>
                          </td>
                          <td style={{ textAlign: 'right', fontWeight: 700 }}>₹{item.value.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                          <td style={{ textAlign: 'right' }}>
                            <span style={{
                              display: 'inline-block', padding: '2px 10px', borderRadius: 99,
                              background: '#EDE9FE', color: '#7C3AED', fontSize: '0.75rem', fontWeight: 700,
                            }}>{pct}%</span>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Tab: Monthly */}
      {activeTab === 1 && (
        <div className="card" style={{ padding: 24 }}>
          <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B', marginBottom: 20 }}>Monthly Spending — 2026</h2>
          <div style={{ height: 320 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={MONTHLY_DATA} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F1F5F9" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94A3B8' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94A3B8' }} tickFormatter={v => `₹${v / 1000}k`} />
                <Tooltip formatter={v => [`₹${v.toLocaleString('en-IN')}`, 'Amount']}
                  contentStyle={{ borderRadius: 10, border: '1px solid #E2E8F0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                  cursor={{ fill: '#F8F7FF' }} />
                <Bar dataKey="value" fill="#7C3AED" radius={[6, 6, 0, 0]} barSize={28} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Tab: Trends */}
      {activeTab === 2 && (
        <div className="card" style={{ padding: 24 }}>
          <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B', marginBottom: 20 }}>Spending Trend</h2>
          <div style={{ height: 320 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={MONTHLY_DATA} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94A3B8' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94A3B8' }} tickFormatter={v => `₹${v / 1000}k`} />
                <Tooltip formatter={v => [`₹${v.toLocaleString('en-IN')}`, 'Amount']}
                  contentStyle={{ borderRadius: 10, border: '1px solid #E2E8F0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }} />
                <Line type="monotone" dataKey="value" stroke="#7C3AED" strokeWidth={3} dot={{ fill: '#7C3AED', r: 4 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
}

export default Reports;
