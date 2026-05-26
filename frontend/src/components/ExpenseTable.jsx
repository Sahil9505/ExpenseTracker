import React from 'react';
import { Link } from 'react-router-dom';

function ExpenseTable({ expenses }) {
  const getIcon = (category) => {
    switch(category) {
      case 'Food': return '🍔';
      case 'Transport': return '🚗';
      case 'Travel': return '✈️';
      case 'Shopping': return '🛍️';
      case 'Entertainment': return '🎟️';
      case 'Bills': return '⚡';
      default: return '📦';
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm flex flex-col h-full">
      <div className="p-6 border-b border-slate-100 flex justify-between items-center">
        <h2 className="text-lg font-bold text-slate-900">Recent Expenses</h2>
        <Link to="/expenses" className="text-sm text-purple-600 hover:text-purple-700 font-semibold transition-colors">
          View All
        </Link>
      </div>
      
      <div className="overflow-x-auto flex-1">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="text-slate-400 text-xs uppercase tracking-wider font-semibold border-b border-slate-100">
              <th className="px-6 py-4">Expense</th>
              <th className="px-6 py-4">Category</th>
              <th className="px-6 py-4">Date</th>
              <th className="px-6 py-4 text-right">Amount</th>
              <th className="px-6 py-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-sm">
            {expenses.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-6 py-8 text-center text-slate-500">No recent transactions.</td>
              </tr>
            ) : (
              expenses.map(exp => (
                <tr key={exp.id} className="hover:bg-slate-50 transition duration-200 group h-16">
                  <td className="px-6 py-2">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center text-lg shadow-sm border border-slate-200">
                        {getIcon(exp.category)}
                      </div>
                      <span className="font-bold text-slate-900">{exp.title}</span>
                    </div>
                  </td>
                  <td className="px-6 py-2">
                    <span className="inline-flex items-center px-3 py-1 rounded-lg text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200">
                      {exp.category}
                    </span>
                  </td>
                  <td className="px-6 py-2 text-slate-500 font-medium">
                    {new Date(exp.date).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                  </td>
                  <td className="px-6 py-2 text-right font-bold text-slate-900 text-base">
                    ₹{exp.amount.toLocaleString()}
                  </td>
                  <td className="px-6 py-2 text-center opacity-100 lg:opacity-0 lg:group-hover:opacity-100 transition-opacity">
                    <div className="flex justify-center gap-3">
                      <button className="text-slate-400 hover:text-purple-600 transition-colors" title="Edit">✏️</button>
                      <button className="text-slate-400 hover:text-red-500 transition-colors" title="Delete">🗑️</button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default ExpenseTable;
