import React from 'react';

function EmptyState({ icon = '📭', title = 'No Data Found', description = 'There is currently nothing to show here.' }) {
  return (
    <div className="flex flex-col items-center justify-center p-8 text-center h-full">
      <div className="text-5xl mb-4 opacity-50">{icon}</div>
      <h3 className="text-lg font-bold text-slate-800 mb-2">{title}</h3>
      <p className="text-slate-500 text-sm max-w-xs mx-auto">
        {description}
      </p>
    </div>
  );
}

export default EmptyState;
