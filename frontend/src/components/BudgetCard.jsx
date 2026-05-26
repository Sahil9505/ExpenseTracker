import React from 'react';
import { Link } from 'react-router-dom';

function BudgetCard({ pieData }) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex flex-col h-full">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg font-bold text-slate-900">Budget Progress</h2>
        <Link to="/budget" className="text-sm text-purple-600 hover:text-purple-700 font-semibold transition-colors">
          Adjust Budgets
        </Link>
      </div>
      
      <div className="flex-1 space-y-5 flex flex-col justify-center">
        {pieData.slice(0, 4).map((cat, i) => {
          const budgetLimit = 5000; // Mock budget limit
          const percent = Math.min(Math.round((cat.value / budgetLimit) * 100), 100);
          const colorClass = ['bg-emerald-500', 'bg-blue-500', 'bg-purple-500', 'bg-orange-500'][i % 4];
          
          return (
            <div key={cat.name}>
              <div className="flex justify-between items-end mb-2">
                <span className="text-sm font-bold text-slate-900">{cat.name}</span>
                <span className="text-xs font-medium text-slate-500">
                  ₹{cat.value.toLocaleString()} / ₹{budgetLimit.toLocaleString()} 
                  <span className={`ml-2 px-2 py-0.5 rounded text-[10px] font-bold ${percent > 90 ? 'bg-red-50 text-red-600' : 'bg-slate-50 text-slate-600'}`}>
                    {percent}%
                  </span>
                </span>
              </div>
              <div className="h-2.5 w-full bg-slate-100 rounded-full overflow-hidden shadow-inner">
                <div className={`h-full ${colorClass} rounded-full transition-all duration-500`} style={{ width: `${percent}%` }}></div>
              </div>
            </div>
          );
        })}
        {pieData.length === 0 && <div className="text-slate-400 text-sm text-center">No active budgets.</div>}
      </div>
    </div>
  );
}

export default BudgetCard;
