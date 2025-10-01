import axios from 'axios';

/**
 * Axios instance configured for Holiday Guard API calls.
 *
 * Features:
 * - Base URL set to '/api/v1' for all API endpoints
 * - Response interceptor that handles 401 Unauthorized errors
 * - Automatically triggers logout/redirect when session expires
 *
 * Usage:
 * ```typescript
 * import api from './services/api';
 *
 * // GET request
 * const response = await api.get('/schedules');
 *
 * // POST request
 * const response = await api.post('/schedules', scheduleData);
 *
 * // PUT request
 * const response = await api.put(`/schedules/${id}`, updatedData);
 *
 * // DELETE request
 * const response = await api.delete(`/schedules/${id}`);
 * ```
 */
const api = axios.create({
  baseURL: '/api/v1',
});

/**
 * Callback function invoked when a 401 Unauthorized response is received.
 * Typically used to redirect the user to the login page when their session expires.
 */
let onUnauthorized: (() => void) | null = null;

/**
 * Registers a callback to be invoked when any API request receives a 401 Unauthorized response.
 * This should be called during app initialization to set up automatic session expiry handling.
 *
 * @param callback - Function to execute on 401 errors (e.g., redirect to login)
 *
 * @example
 * ```typescript
 * setOnUnauthorized(() => {
 *   // Clear auth state and redirect to login
 *   navigate('/login');
 * });
 * ```
 */
export const setOnUnauthorized = (callback: () => void) => {
  onUnauthorized = callback;
};

/**
 * Response interceptor that catches 401 Unauthorized errors and triggers
 * the registered onUnauthorized callback (e.g., redirect to login page).
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      if (onUnauthorized) {
        onUnauthorized();
      }
    }
    return Promise.reject(error);
  }
);

export default api;
