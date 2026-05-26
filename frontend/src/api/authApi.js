import api from './api';

/**
 * authApi.js - API functions for authentication.
 * These call the Spring Boot /api/auth/* endpoints.
 */

// Register a new user
export const registerUser = (name, email, password) => {
  return api.post('/api/auth/register', { name, email, password });
};

// Login and get JWT token
export const loginUser = (email, password) => {
  return api.post('/api/auth/login', { email, password });
};
