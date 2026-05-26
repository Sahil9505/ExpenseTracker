import api from './api';

/**
 * expenseApi.js - API functions for expense CRUD operations.
 * These call the Spring Boot /api/expenses/* endpoints.
 * JWT token is automatically attached by the Axios interceptor in api.js.
 */

// Get all expenses for current user
export const getExpenses = () => {
  return api.get('/api/expenses');
};

// Add a new expense
export const addExpense = (expenseData) => {
  return api.post('/api/expenses', expenseData);
};

// Update an expense by ID
export const updateExpense = (id, expenseData) => {
  return api.put(`/api/expenses/${id}`, expenseData);
};

// Delete an expense by ID
export const deleteExpense = (id) => {
  return api.delete(`/api/expenses/${id}`);
};

// Get dashboard summary (total + category breakdown)
export const getExpenseSummary = () => {
  return api.get('/api/expenses/summary');
};

// Get budget status
export const getBudgetStatus = (month, year) => {
  return api.get('/api/budgets/status', { params: { month, year } });
};

// Get all categories
export const getCategories = () => {
  return api.get('/api/categories');
};

// Get monthly report for a specific year
export const getMonthlyReport = (year) => {
  return api.get('/api/reports/monthly', { params: { year } });
};
