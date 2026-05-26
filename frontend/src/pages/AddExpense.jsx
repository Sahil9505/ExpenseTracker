import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { addExpense, updateExpense } from '../api/expenseApi';
import Spinner from '../components/Spinner';

const CATEGORIES = ['Food', 'Travel', 'Shopping', 'Bills', 'Entertainment', 'Health', 'Transport', 'Education', 'Other'];
const CATEGORY_ICONS = {
  Food: '🍔', Travel: '✈️', Shopping: '🛍️', Bills: '⚡', Entertainment: '🎬',
  Health: '💊', Transport: '🚗', Education: '📚', Other: '📦',
};

const labelStyle = { display: 'block', fontSize: '0.78rem', fontWeight: 700, color: '#374151', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' };

function AddExpense({ expenseToEdit, onCancel, onSave, isModal }) {
  const navigate = useNavigate();
  const isEditMode = !!expenseToEdit;

  const [title, setTitle] = useState(expenseToEdit?.title || '');
  const [amount, setAmount] = useState(expenseToEdit?.amount || '');
  const [category, setCategory] = useState(expenseToEdit?.category || 'Food');
  const [date, setDate] = useState(expenseToEdit?.date || new Date().toISOString().split('T')[0]);
  const [description, setDescription] = useState(expenseToEdit?.description || '');
  const [paymentMethod, setPaymentMethod] = useState(expenseToEdit?.paymentMethod || 'Card');
  const [isRecurring, setIsRecurring] = useState(expenseToEdit?.isRecurring || false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (title.trim().length < 3) { toast.error('Title must be at least 3 characters'); return; }
    if (parseFloat(amount) <= 0) { toast.error('Amount must be greater than 0'); return; }

    setLoading(true);
    const payload = { title, amount: parseFloat(amount), category, date, description, paymentMethod, isRecurring };
    try {
      if (isEditMode) {
        await updateExpense(expenseToEdit.id, payload);
        toast.success('Expense updated!');
      } else {
        await addExpense(payload);
        toast.success('Expense added!');
      }
      if (onSave) onSave();
      if (!isEditMode) navigate('/expenses');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to save expense');
    } finally {
      setLoading(false);
    }
  };

  const formContent = (
    <div className={`card ${isModal ? '' : ''}`} style={{ padding: isModal ? 28 : 32, width: '100%' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 28 }}>
        <div style={{
          width: 40, height: 40, borderRadius: 10,
          background: isEditMode ? '#FEF3C7' : '#EDE9FE',
          display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18,
        }}>
          {isEditMode ? '✏️' : '➕'}
        </div>
        <div>
          <h2 style={{ fontWeight: 800, fontSize: '1.1rem', color: '#1E293B' }}>
            {isEditMode ? 'Edit Expense' : 'Add New Expense'}
          </h2>
          <p style={{ fontSize: '0.8rem', color: '#64748B', marginTop: 2 }}>
            {isEditMode ? 'Update the expense details below.' : 'Fill in the details to record a new transaction.'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        {/* Title — full width */}
        <div style={{ marginBottom: 20 }}>
          <label style={labelStyle}>Title *</label>
          <input
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder="e.g., Dinner at Restaurant"
            required
            className="input"
          />
        </div>

        {/* Two-column grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: isModal ? '1fr' : 'repeat(auto-fill, minmax(220px, 1fr))',
          gap: 20,
          marginBottom: 20,
        }}>
          {/* Left: Amount */}
          <div>
            <label style={labelStyle}>Amount (₹) *</label>
            <div style={{ position: 'relative' }}>
              <span style={{
                position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)',
                color: '#7C3AED', fontWeight: 700, fontSize: '1rem', pointerEvents: 'none',
              }}>₹</span>
              <input
                type="number"
                value={amount}
                onChange={e => setAmount(e.target.value)}
                placeholder="0.00"
                min="0.01"
                step="0.01"
                required
                className="input"
                style={{ paddingLeft: 28 }}
              />
            </div>
          </div>

          {/* Category */}
          <div>
            <label style={labelStyle}>Category *</label>
            <select value={category} onChange={e => setCategory(e.target.value)} className="select">
              {CATEGORIES.map(c => <option key={c} value={c}>{CATEGORY_ICONS[c]} {c}</option>)}
            </select>
          </div>

          {/* Payment Method */}
          <div>
            <label style={labelStyle}>Payment Method</label>
            <select value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)} className="select">
              <option value="Card">💳 Card</option>
              <option value="Cash">💵 Cash</option>
              <option value="UPI">📱 UPI</option>
              <option value="Bank Transfer">🏦 Bank Transfer</option>
            </select>
          </div>

          {/* Date */}
          <div>
            <label style={labelStyle}>Date *</label>
            <input
              type="date"
              value={date}
              onChange={e => setDate(e.target.value)}
              required
              className="input"
            />
          </div>
        </div>

        {/* Description */}
        <div style={{ marginBottom: 20 }}>
          <label style={labelStyle}>Notes / Description</label>
          <textarea
            value={description}
            onChange={e => setDescription(e.target.value)}
            placeholder="Add an optional note..."
            rows={3}
            className="textarea"
            style={{ resize: 'none' }}
          />
        </div>

        {/* Recurring toggle */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 12,
          padding: '12px 16px', borderRadius: 10, border: '1px solid #E2E8F0', background: '#F8F7FF',
          marginBottom: 24,
        }}>
          <input
            type="checkbox"
            id="recurring"
            checked={isRecurring}
            onChange={e => setIsRecurring(e.target.checked)}
            style={{ width: 18, height: 18, cursor: 'pointer', accentColor: '#7C3AED' }}
          />
          <label htmlFor="recurring" style={{ fontSize: '0.875rem', fontWeight: 500, color: '#374151', cursor: 'pointer', userSelect: 'none' }}>
            Mark as monthly recurring expense
          </label>
        </div>

        {/* Actions */}
        <div style={{
          display: 'flex', gap: 12, paddingTop: 20, borderTop: '1px solid #F1F5F9',
          flexWrap: 'wrap',
        }}>
          {onCancel && (
            <button type="button" onClick={onCancel} className="btn-secondary" style={{ flex: '1 1 120px' }}>
              Cancel
            </button>
          )}
          <button
            type="submit"
            disabled={loading}
            className="btn-primary"
            style={{ flex: '2 1 180px' }}
          >
            {loading && <Spinner size="sm" />}
            {loading ? 'Saving...' : (isEditMode ? 'Update Expense' : 'Save Expense')}
          </button>
        </div>
      </form>
    </div>
  );

  if (isModal) return formContent;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div>
        <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#1E293B', letterSpacing: '-0.02em' }}>Add New Expense</h1>
        <p style={{ fontSize: '0.875rem', color: '#64748B', marginTop: 4 }}>Record a new transaction to track your spending.</p>
      </div>
      {formContent}
    </div>
  );
}

export default AddExpense;
