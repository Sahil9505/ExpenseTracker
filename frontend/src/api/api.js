import axios from 'axios';

/**
 * api.js - Centralized Axios configuration.
 *
 * All API calls go through this instance so that:
 * 1. The base URL is set once
 * 2. The JWT token is automatically added to every request
 */

// Create an Axios instance with our backend URL
const api = axios.create({
  baseURL: 'http://localhost:8080', // Spring Boot backend
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor:
 * Before every request, check if we have a JWT token in localStorage.
 * If yes, add it to the Authorization header.
 *
 * This means we don't have to manually add the token in every API call.
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token'); // Get JWT from localStorage
  if (token) {
    config.headers.Authorization = `Bearer ${token}`; // Add to header
  }
  return config;
});

/**
 * Response Interceptor:
 * If the server returns 401 (Unauthorized), the token is expired or invalid.
 * Automatically log out the user and redirect to login.
 */
api.interceptors.response.use(
  (response) => {
    // If the backend returns our standard ApiResponse format, 
    // we return the JSON payload directly.
    // This means components receive { success, message, data }
    // and can access the payload via res.data natively!
    if (response.data && response.data.success !== undefined) {
      return response.data;
    }
    return response;
  },
  (error) => {
    // Extract our custom error message from the ApiResponse format if available
    if (error.response?.data?.message) {
      error.message = error.response.data.message;
    }
    
    if (error.response?.status === 401) {
      localStorage.removeItem('token');    // Clear invalid token
      localStorage.removeItem('username');
      window.location.href = '/login';     // Redirect to login
    }
    return Promise.reject(error);
  }
);

export default api;