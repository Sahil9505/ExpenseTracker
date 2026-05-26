import React from 'react';

function SummaryCard({ highestExpense, lowestExpense, topCategory }) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex flex-col h-full">
      <h2 className="text-lg font-bold text-slate-900 mb-6">Quick Insights</h2>
      
      <div className="space-y-4 flex-1 flex flex-col justify-center">
        {/* Highest Expense */}
        <div className="flex items-center justify-between p-4 rounded-xl border border-slate-100 bg-slate-50 transition-colors hover:bg-white hover:border-slate-200 hover:shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-red-50 text-red-600 flex items-center justify-center text-xl shadow-sm border border-red-100">↗️</div>
            <div>
              <p className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-0.5">Highest Expense</p>
              <p className="text-sm font-bold text-slate-900 truncate max-w-[140px]">{highestExpense.title}</p>
            </div>
          </div>
          <p className="text-lg font-bold text-red-600">₹{highestExpense.amount.toLocaleString()}</p>
        </div>

        {/* Lowest Expense */}
        <div className="flex items-center justify-between p-4 rounded-xl border border-slate-100 bg-slate-50 transition-colors hover:bg-white hover:border-slate-200 hover:shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl shadow-sm border border-emerald-100">↘️</div>
            <div>
              <p className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-0.5">Lowest Expense</p>
              <p className="text-sm font-bold text-slate-900 truncate max-w-[140px]">{lowestExpense.title}</p>
            </div>
          </div>
          <p className="text-lg font-bold text-emerald-600">₹{lowestExpense.amount.toLocaleString()}</p>
        </div>

        {/* Top Category */}
        <div className="flex items-center justify-between p-4 rounded-xl border border-slate-100 bg-slate-50 transition-colors hover:bg-white hover:border-slate-200 hover:shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center text-xl shadow-sm border border-purple-100">🎯</div>
            <div>
              <p className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-0.5">Top Category</p>
              <p className="text-sm font-bold text-slate-900">{topCategory.category}</p>
            </div>
          </div>
          <p className="text-lg font-bold text-purple-600">₹{topCategory.total.toLocaleString()}</p>
        </div>
      </div>
    </div>
  );
}

export default SummaryCard;
