import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { getExpenses, deleteExpense } from '../api/expenseApi';
import AddExpense from './AddExpense';
import Spinner from '../components/Spinner';

const ITEMS_PER_PAGE = 10;

const CATEGORY_ICONS = {
  Food: '🍔', Travel: '✈️', Shopping: '🛍️', Bills: '⚡', Entertainment: '🎬',
  Health: '💊', Transport: '🚗', Education: '📚', Other: '📦',
};

const PAYMENT_ICONS = { Card: '💳', Cash: '💵', UPI: '📱', 'Bank Transfer': '🏦' };

function AllExpenses() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [sortOrder, setSortOrder] = useState('desc');
  const [editingExpense, setEditingExpense] = useState(null);
  const [expenseToDelete, setExpenseToDelete] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await getExpenses();
      setExpenses(res.data || []);
    } catch {
      toast.error('Failed to load expenses.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (id) => {
    const expense = expenses.find(e => e.id === id);
    setExpenseToDelete(expense);
  };

  const confirmDelete = async () => {
    if (!expenseToDelete) return;
    try {
      await deleteExpense(expenseToDelete.id);
      toast.success('Expense deleted!');
      // Update local state directly instead of full fetch
      setExpenses(prev => prev.filter(e => e.id !== expenseToDelete.id));
      setExpenseToDelete(null);
    } catch (err) {
      toast.error(err.message || 'Failed to delete');
    }
  };

  const filtered = expenses
    .filter(e =>
      e.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (e.description && e.description.toLowerCase().includes(searchQuery.toLowerCase()))
    )
    .filter(e => !categoryFilter || e.category === categoryFilter)
    .sort((a, b) => {
      const da = new Date(a.date).getTime(), db = new Date(b.date).getTime();
      return sortOrder === 'desc' ? db - da : da - db;
    });

  const totalPages = Math.max(1, Math.ceil(filtered.length / ITEMS_PER_PAGE));
  const paginated = filtered.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);
  const uniqueCategories = [...new Set(expenses.map(e => e.category))];

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', flexDirection: 'column', gap: 12 }}>
        <Spinner size="lg" />
        <p style={{ color: '#64748B', fontWeight: 500 }}>Loading expenses...</p>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Page header */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>All Expenses</h1>
          <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Manage and review all your transactions.</p>
        </div>
      </div>

      {/* Filters row */}
      <div className="card" style={{ padding: '16px 20px', display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center' }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: '1 1 220px', minWidth: 0 }}>
          <svg width="16" height="16" fill="none" stroke="#94A3B8" strokeWidth="2" viewBox="0 0 24 24"
            style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }}>
            <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            type="text"
            placeholder="Search expenses..."
            value={searchQuery}
            onChange={e => { setSearchQuery(e.target.value); setCurrentPage(1); }}
            className="input"
            style={{ paddingLeft: 38 }}
          />
        </div>

        {/* Category filter */}
        <select
          value={categoryFilter}
          onChange={e => { setCategoryFilter(e.target.value); setCurrentPage(1); }}
          className="select"
          style={{ flex: '0 0 160px' }}
        >
          <option value="">All Categories</option>
          {uniqueCategories.map(c => <option key={c} value={c}>{c}</option>)}
        </select>

        {/* Sort */}
        <select
          value={sortOrder}
          onChange={e => setSortOrder(e.target.value)}
          className="select"
          style={{ flex: '0 0 150px' }}
        >
          <option value="desc">Newest First</option>
          <option value="asc">Oldest First</option>
        </select>

        <span style={{
          marginLeft: 'auto', fontSize: '0.8rem', fontWeight: 600,
          background: '#EDE9FE', color: '#7C3AED', padding: '4px 12px', borderRadius: 99,
        }}>
          {filtered.length} records
        </span>
      </div>

      {/* Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Category</th>
                <th>Payment</th>
                <th style={{ textAlign: 'right' }}>Amount</th>
                <th style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginated.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ padding: '60px 20px', textAlign: 'center' }}>
                    <div style={{ color: '#94A3B8', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
                      <div style={{ fontSize: 40 }}>📭</div>
                      <p style={{ fontWeight: 600, color: '#64748B' }}>
                        {expenses.length === 0 ? "No expenses yet." : "No matches found."}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                paginated.map(exp => (
                  <tr key={exp.id}>
                    <td style={{ whiteSpace: 'nowrap', color: '#64748B', fontSize: '0.8rem', fontWeight: 500 }}>
                      {new Date(exp.date).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </td>
                    <td>
                      <p style={{ fontWeight: 600, fontSize: '0.875rem', color: '#1E293B' }}>{exp.title}</p>
                      {exp.description && <p style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: 2 }}>{exp.description}</p>}
                    </td>
                    <td>
                      <span style={{
                        display: 'inline-flex', alignItems: 'center', gap: 5,
                        fontSize: '0.75rem', fontWeight: 600,
                        background: '#F1F5F9', color: '#475569',
                        padding: '3px 10px', borderRadius: 6,
                      }}>
                        {CATEGORY_ICONS[exp.category] || '📦'} {exp.category}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.8rem', color: '#64748B', fontWeight: 500 }}>
                        {PAYMENT_ICONS[exp.paymentMethod] || ''} {exp.paymentMethod || '—'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 700, fontSize: '0.9rem', color: '#1E293B', whiteSpace: 'nowrap' }}>
                      ₹{exp.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <div style={{ display: 'flex', justifyContent: 'center', gap: 8 }}>
                        <button
                          onClick={() => setEditingExpense(exp)}
                          title="Edit"
                          style={{
                            background: '#EDE9FE', border: 'none', borderRadius: 6, padding: '6px 8px',
                            cursor: 'pointer', color: '#7C3AED', display: 'flex', alignItems: 'center',
                            transition: 'background 0.15s',
                          }}
                          onMouseEnter={e => e.currentTarget.style.background = '#DDD6FE'}
                          onMouseLeave={e => e.currentTarget.style.background = '#EDE9FE'}
                        >
                          <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                            <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                            <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                          </svg>
                        </button>
                        <button
                          onClick={() => handleDelete(exp.id)}
                          title="Delete"
                          style={{
                            background: '#FEE2E2', border: 'none', borderRadius: 6, padding: '6px 8px',
                            cursor: 'pointer', color: '#DC2626', display: 'flex', alignItems: 'center',
                            transition: 'background 0.15s',
                          }}
                          onMouseEnter={e => e.currentTarget.style.background = '#FECACA'}
                          onMouseLeave={e => e.currentTarget.style.background = '#FEE2E2'}
                        >
                          <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                            <polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14H6L5 6" />
                            <path d="M10 11v6M14 11v6M9 6V4h6v2" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div style={{
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            padding: '14px 20px', borderTop: '1px solid #F1F5F9',
            flexWrap: 'wrap', gap: 8,
          }}>
            <span style={{ fontSize: '0.8rem', color: '#64748B' }}>
              Page {currentPage} of {totalPages} ({filtered.length} total)
            </span>
            <div style={{ display: 'flex', gap: 6 }}>
              <button
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                style={{
                  padding: '6px 14px', borderRadius: 8, border: '1px solid #E2E8F0',
                  background: '#fff', cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
                  fontSize: '0.8rem', fontWeight: 600, color: currentPage === 1 ? '#CBD5E1' : '#374151',
                }}
              >← Prev</button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(p => (
                <button
                  key={p}
                  onClick={() => setCurrentPage(p)}
                  style={{
                    width: 34, height: 34, borderRadius: 8, border: '1px solid',
                    borderColor: p === currentPage ? '#7C3AED' : '#E2E8F0',
                    background: p === currentPage ? '#7C3AED' : '#fff',
                    color: p === currentPage ? '#fff' : '#374151',
                    cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600,
                  }}
                >{p}</button>
              ))}
              <button
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                style={{
                  padding: '6px 14px', borderRadius: 8, border: '1px solid #E2E8F0',
                  background: '#fff', cursor: currentPage === totalPages ? 'not-allowed' : 'pointer',
                  fontSize: '0.8rem', fontWeight: 600, color: currentPage === totalPages ? '#CBD5E1' : '#374151',
                }}
              >Next →</button>
            </div>
          </div>
        )}
      </div>

      {/* Edit Modal */}
      {editingExpense && (
        <div style={{
          position: 'fixed', inset: 0,
          background: 'rgba(15,23,42,0.55)', backdropFilter: 'blur(4px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          padding: 16, zIndex: 50,
        }}>
          <div style={{ width: '100%', maxWidth: 560 }}>
            <AddExpense
              expenseToEdit={editingExpense}
              onCancel={() => setEditingExpense(null)}
              onSave={() => { setEditingExpense(null); fetchData(); }}
              isModal
            />
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {expenseToDelete && (
        <div style={{
          position: 'fixed', inset: 0,
          background: 'rgba(15,23,42,0.55)', backdropFilter: 'blur(4px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          padding: 16, zIndex: 100,
        }}>
          <div className="card" style={{ width: '100%', maxWidth: 400, padding: 24, textAlign: 'center' }}>
            <div style={{ fontSize: 40, marginBottom: 16 }}>⚠️</div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 800, color: '#1E293B', marginBottom: 10 }}>Confirm Deletion</h3>
            <p style={{ color: '#64748B', fontSize: '0.9rem', marginBottom: 24 }}>
              Are you sure you want to delete <strong>{expenseToDelete.title}</strong>? This action cannot be undone.
            </p>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
              <button className="btn-secondary" onClick={() => setExpenseToDelete(null)} style={{ flex: 1 }}>
                Cancel
              </button>
              <button 
                className="btn-primary" 
                onClick={confirmDelete}
                style={{ flex: 1, background: '#DC2626' }}
                onMouseEnter={e => e.currentTarget.style.background = '#B91C1C'}
                onMouseLeave={e => e.currentTarget.style.background = '#DC2626'}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AllExpenses;
