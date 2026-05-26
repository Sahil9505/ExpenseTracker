import React from 'react';

function ChartCard({ title, children, action }) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex flex-col min-h-[380px]">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg font-bold text-slate-900">{title}</h2>
        {action && <div>{action}</div>}
      </div>
      <div className="flex-1 w-full h-full relative">
        {children}
      </div>
    </div>
  );
}

export default ChartCard;
