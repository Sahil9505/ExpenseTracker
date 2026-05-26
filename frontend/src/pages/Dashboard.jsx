import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  PieChart, Pie, Cell,
} from 'recharts';
import { getExpenses, getExpenseSummary, getBudgetStatus, getCategories, getMonthlyReport } from '../api/expenseApi';
import Spinner from '../components/Spinner';

const PIE_COLORS = ['#7C3AED', '#10B981', '#F59E0B', '#3B82F6', '#EF4444', '#06B6D4'];

const CATEGORY_ICONS = {
  Food: '🍔', Travel: '✈️', Shopping: '🛍️', Bills: '⚡', Entertainment: '🎬',
  Health: '💊', Transport: '🚗', Education: '📚', Other: '📦',
};

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

function StatCard({ icon, iconBg, title, value, badge, badgeColor = '#10B981' }) {
  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{
          width: 44, height: 44, borderRadius: 12,
          background: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 20,
        }}>{icon}</div>
        {badge && (
          <span style={{
            fontSize: '0.72rem', fontWeight: 700, padding: '3px 10px', borderRadius: 99,
            background: badgeColor === 'up' ? '#D1FAE5' : '#FEE2E2',
            color: badgeColor === 'up' ? '#065F46' : '#991B1B',
          }}>{badge}</span>
        )}
      </div>
      <div>
        <p style={{ fontSize: '0.8rem', color: '#64748B', fontWeight: 500 }}>{title}</p>
        <h3 style={{ fontSize: '1.65rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.03em', marginTop: 2 }}>{value}</h3>
      </div>
    </div>
  );
}

function Dashboard() {
  const [expenses, setExpenses] = useState([]);
  const [summary, setSummary] = useState(null);
  const [budgetRemaining, setBudgetRemaining] = useState(0);
  const [categoriesCount, setCategoriesCount] = useState(0);
  const [monthlyReports, setMonthlyReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const username = localStorage.getItem('username') || 'User';

  useEffect(() => { fetchData(); }, []);
  const currentYear = new Date().getFullYear();

  const fetchData = async () => {
    try {
      const [expRes, sumRes, budgetRes, catRes, reportRes] = await Promise.all([
        getExpenses(),
        getExpenseSummary(),
        getBudgetStatus(),
        getCategories(),
        getMonthlyReport(currentYear)
      ]);

      const expData = expRes.data || [];
      const sumData = sumRes.data || {};
      const budgetData = budgetRes.data || [];
      const categoryData = catRes.data || [];
      const reportData = reportRes.data || [];

      setExpenses(expData);
      setSummary(sumData);

      // Calculate Budget Remaining
      // If backend has custom budgets, use them. Otherwise fallback to app defaults.
      if (budgetData.length > 0) {
        const totalRemaining = budgetData.reduce((acc, curr) => acc + (curr.remaining || 0), 0);
        setBudgetRemaining(totalRemaining);
      } else {
        const totalMonthlyBudget = Object.values(BUDGETS).reduce((s, b) => s + b.limit, 0);
        const spentThisMonth = sumData.totalThisMonth || 0;
        setBudgetRemaining(Math.max(0, totalMonthlyBudget - spentThisMonth));
      }

      // Category Count: Show created categories + default categories (if not already custom)
      // For consistency with Categories page, we'll show the count of default categories
      setCategoriesCount(Object.keys(CATEGORY_ICONS).length);
      setMonthlyReports(reportData);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  if (loading || !summary) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '60vh', gap: 12 }}>
        <Spinner size="lg" />
        <p style={{ color: '#64748B', fontWeight: 500 }}>Loading dashboard...</p>
      </div>
    );
  }

  const recentExpenses = [...expenses].slice(0, 6);
  const pieData = (summary.categoryTotals || []).map(item => ({ name: item.category, value: item.total }));

  // Map monthly reports (from API) to chart data format
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const barData = monthNames.map((name, index) => {
    const monthNum = index + 1;
    const report = monthlyReports.find(r => r.month === monthNum);
    return { name, value: report ? report.total : 0 };
  });

  const topCategory = (summary.categoryTotals || []).reduce(
    (prev, c) => (c.total > prev.total ? c : prev), { category: '-', total: 0 }
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>

      {/* Hero header */}
      <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12, padding: '20px 24px' }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>
            Good morning, {username}! 👋
          </h1>
          <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>
            Here's a summary of your finances today.
          </p>
        </div>
        <Link to="/add-expense" className="btn-primary">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Add Expense
        </Link>
      </div>

      {/* 4 Stat cards — 2×2 on desktop */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))',
        gap: 20,
      }}>
        <StatCard icon="💼" iconBg="#EDE9FE" title="Total Expenses" value={`₹${(summary.totalAllTime || 0).toLocaleString('en-IN')}`} badge="↑ 12%" badgeColor="up" />
        <StatCard icon="📅" iconBg="#D1FAE5" title="Monthly Expenses" value={`₹${(summary.totalThisMonth || 0).toLocaleString('en-IN')}`} badge="↑ 8%" badgeColor="up" />
        <StatCard icon="🎯" iconBg="#FEF3C7" title="Budget Remaining" value={`₹${budgetRemaining.toLocaleString('en-IN')}`} badge="On track" badgeColor="up" />
        <StatCard icon="🗂️" iconBg="#DBEAFE" title="Categories" value={categoriesCount} />
      </div>

      {/* Charts row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 20 }}>
        {/* Bar Chart */}
        <div className="card" style={{ padding: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
            <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B' }}>Monthly Spending Trend</h2>
            <span style={{ fontSize: '0.72rem', color: '#7C3AED', fontWeight: 600 }}>2026</span>
          </div>
          <div style={{ height: 220 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData} margin={{ top: 5, right: 5, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F1F5F9" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#94A3B8', fontWeight: 500 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#94A3B8' }} tickFormatter={v => `₹${v / 1000}k`} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#fff', border: '1px solid #E2E8F0', borderRadius: 10, boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                  formatter={v => [`₹${v.toLocaleString('en-IN')}`, 'Amount']}
                  cursor={{ fill: '#F8F7FF' }}
                />
                <Bar dataKey="value" fill="#7C3AED" radius={[6, 6, 0, 0]} barSize={22} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Pie / Category distribution */}
        <div className="card" style={{ padding: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
            <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B' }}>Category Breakdown</h2>
          </div>
          {pieData.length === 0 ? (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 220, color: '#94A3B8', fontSize: '0.875rem' }}>
              No data yet
            </div>
          ) : (
            <div style={{ height: 220, position: 'relative' }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={65} outerRadius={90} paddingAngle={3} dataKey="value" stroke="none">
                    {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Pie>
                  <Tooltip
                    formatter={v => [`₹${v.toLocaleString('en-IN')}`, '']}
                    contentStyle={{ backgroundColor: '#fff', border: '1px solid #E2E8F0', borderRadius: 10, boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div style={{
                position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column',
                alignItems: 'center', justifyContent: 'center', pointerEvents: 'none',
              }}>
                <span style={{ fontSize: '0.7rem', fontWeight: 700, color: '#94A3B8', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Top</span>
                <span style={{ fontSize: '1rem', fontWeight: 700, color: '#1E293B', marginTop: 2 }}>{topCategory.category}</span>
              </div>
            </div>
          )}
          {/* Legend */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px 16px', marginTop: 12 }}>
            {pieData.map((item, i) => (
              <div key={item.name} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: PIE_COLORS[i % PIE_COLORS.length], flexShrink: 0 }} />
                <span style={{ fontSize: '0.75rem', color: '#64748B', fontWeight: 500 }}>{item.name}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '18px 20px', borderBottom: '1px solid #F1F5F9' }}>
          <h2 style={{ fontWeight: 700, fontSize: '1rem', color: '#1E293B' }}>Recent Transactions</h2>
          <Link to="/expenses" style={{ fontSize: '0.8rem', color: '#7C3AED', fontWeight: 600, textDecoration: 'none' }}>
            View all →
          </Link>
        </div>
        {recentExpenses.length === 0 ? (
          <div style={{ padding: 40, textAlign: 'center', color: '#94A3B8' }}>
            <div style={{ fontSize: 40, marginBottom: 8 }}>📭</div>
            <p>No transactions yet. <Link to="/add-expense" style={{ color: '#7C3AED' }}>Add one</Link></p>
          </div>
        ) : (
          <div>
            {recentExpenses.map((exp, i) => (
              <div key={exp.id} style={{
                display: 'flex', alignItems: 'center', gap: 14,
                padding: '14px 20px',
                borderBottom: i < recentExpenses.length - 1 ? '1px solid #F8FAFC' : 'none',
                transition: 'background 0.15s',
              }}
                onMouseEnter={e => e.currentTarget.style.background = '#FAFBFE'}
                onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
              >
                <div style={{
                  width: 40, height: 40, borderRadius: 10, flexShrink: 0,
                  background: '#EDE9FE', display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 18,
                }}>
                  {CATEGORY_ICONS[exp.category] || '📦'}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ fontWeight: 600, fontSize: '0.875rem', color: '#1E293B', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{exp.title}</p>
                  <p style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: 2 }}>
                    {new Date(exp.date).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                    {' · '}{exp.category}
                  </p>
                </div>
                <span style={{ fontWeight: 700, fontSize: '0.95rem', color: '#1E293B', flexShrink: 0 }}>
                  − ₹{exp.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
}

export default Dashboard;
