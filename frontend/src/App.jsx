import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import AllExpenses from './pages/AllExpenses';
import AddExpense from './pages/AddExpense';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import Budget from './pages/Budget';
import Categories from './pages/Categories';
import Recurring from './pages/Recurring';
import Bills from './pages/Bills';
import Layout from './components/Layout';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return children;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/dashboard"   element={<ProtectedRoute><Layout><Dashboard /></Layout></ProtectedRoute>} />
        <Route path="/expenses"    element={<ProtectedRoute><Layout><AllExpenses /></Layout></ProtectedRoute>} />
        <Route path="/add-expense" element={<ProtectedRoute><Layout><AddExpense /></Layout></ProtectedRoute>} />
        <Route path="/categories"  element={<ProtectedRoute><Layout><Categories /></Layout></ProtectedRoute>} />
        <Route path="/reports"     element={<ProtectedRoute><Layout><Reports /></Layout></ProtectedRoute>} />
        <Route path="/budget"      element={<ProtectedRoute><Layout><Budget /></Layout></ProtectedRoute>} />
        <Route path="/recurring"   element={<ProtectedRoute><Layout><Recurring /></Layout></ProtectedRoute>} />
        <Route path="/bills"       element={<ProtectedRoute><Layout><Bills /></Layout></ProtectedRoute>} />
        <Route path="/settings"    element={<ProtectedRoute><Layout><Settings /></Layout></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>

      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3500,
          style: {
            borderRadius: '10px',
            background: '#1E293B',
            color: '#fff',
            fontSize: '0.875rem',
            fontWeight: 500,
            boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
          },
          success: {
            iconTheme: { primary: '#10B981', secondary: '#fff' },
          },
          error: {
            iconTheme: { primary: '#EF4444', secondary: '#fff' },
          },
        }}
      />
    </BrowserRouter>
  );
}

export default App;
