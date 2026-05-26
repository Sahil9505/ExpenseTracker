import React from 'react';

const sizes = { sm: 16, md: 28, lg: 44 };

export default function Spinner({ size = 'md' }) {
  const px = sizes[size] || 28;
  const border = size === 'sm' ? 2 : 3;
  return (
    <div
      role="status"
      aria-label="Loading"
      style={{
        width: px, height: px,
        borderRadius: '50%',
        border: `${border}px solid #EDE9FE`,
        borderTopColor: '#7C3AED',
        animation: 'spin 0.7s linear infinite',
        flexShrink: 0,
      }}
    />
  );
}

// Inject keyframes once (CSS-in-JS safe approach)
if (typeof document !== 'undefined' && !document.getElementById('spinner-kf')) {
  const style = document.createElement('style');
  style.id = 'spinner-kf';
  style.textContent = '@keyframes spin { to { transform: rotate(360deg); } }';
  document.head.appendChild(style);
}
