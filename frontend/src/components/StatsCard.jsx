import React from 'react';

function StatsCard({ title, value, icon, trend, iconColorClass = 'text-purple-600 bg-purple-50' }) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all duration-200 flex flex-col justify-between gap-4">
      <div className="flex items-center justify-between">
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl shadow-sm ${iconColorClass}`}>
          {icon}
        </div>
        {trend && (
          <span className="inline-flex items-center px-2.5 py-1 rounded-md text-[11px] font-bold bg-slate-50 text-slate-500 border border-slate-100">
            {trend}
          </span>
        )}
      </div>
      <div>
        <p className="text-sm font-medium text-slate-500 mb-1">{title}</p>
        <h3 className="text-3xl font-bold text-slate-900">{value}</h3>
      </div>
    </div>
  );
}

export default StatsCard;
