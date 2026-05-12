const API_BASE = import.meta.env.VITE_API_BASE || '/gateway';

const parseJson = async (response) => {
  /*
   * Backend responses in this project are mixed:
   * - Some endpoints return JSON objects/lists, such as deliveries and tracking.
   * - Some endpoints return plain strings, such as signup success messages.
   *
   * Because of that, the response is first read as text. Then we try to parse it
   * as JSON. If JSON parsing fails, the original plain text is returned. This
   * keeps the API layer flexible without forcing every backend endpoint to have
   * exactly the same response format.
   */
  const text = await response.text();
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return text;
  }
};

const request = async (path, token, options = {}) => {
  /*
   * This is the common request helper used by every API function below.
   *
   * Why it is useful:
   * - All requests automatically use the same API base path.
   * - JSON headers are added consistently.
   * - JWT token is attached for protected APIs.
   * - Response parsing and error handling stay in one place.
   *
   * Without this helper, every page would repeat fetch(), headers, token logic,
   * response parsing, and error handling.
   */
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    /*
     * Protected backend routes expect JWT in this format:
     * Authorization: Bearer <token>
     *
     * The API Gateway reads this token, validates it, extracts email/role, and
     * forwards those values to backend services using X-User-Email and
     * X-User-Role headers.
     */
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  const data = await parseJson(response);
  if (!response.ok) {
    throw new Error(data?.message || data || `Request failed: ${response.status}`);
  }
  return data;
};

export const decodeJwt = (token) => {
  /*
   * This function decodes only the payload part of the JWT so the frontend can
   * read values like role. It is used for UI behavior, for example showing the
   * Admin Dashboard button only to ADMIN users.
   *
   * This does not verify the JWT signature. Signature verification belongs on
   * the backend gateway, because frontend code can be modified by users.
   */
  if (!token) return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload;
  } catch {
    return null;
  }
};

export const login = async (email, password) => {
  /*
   * Login is a public API because the user does not have a token yet.
   * It sends email/password to auth-service through the gateway and receives
   * a JWT token if credentials are valid.
   */
  return request('/auth/auth/login', null, {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
};

export const signup = async (payload) => {
  /*
   * Signup sends registration data to auth-service.
   * The backend checks duplicate email, encodes the password, assigns role,
   * and saves the user in PostgreSQL.
   */
  return request('/auth/auth/signup', null, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
};

export const getTracking = async (trackingNumber, token) => {
  /*
   * Tracking history is fetched by tracking number.
   * The token is passed because tracking-service allows only authenticated
   * USER or ADMIN roles to view tracking events.
   */
  return request(`/tracking/tracking/${encodeURIComponent(trackingNumber)}`, token, {
    method: 'GET',
  });
};

export const getMyDeliveries = async (token) => {
  /*
   * "My deliveries" does not send an email from the frontend.
   * The backend gets the logged-in email from X-User-Email, which the gateway
   * creates after validating the JWT. This prevents users from simply typing
   * another email in the frontend to access someone else's deliveries.
   */
  return request('/deliveries/deliveries/my', token, {
    method: 'GET',
  });
};

export const createDelivery = async (payload, token) => {
  /*
   * Creates a new delivery using the nested structure expected by delivery-service:
   * {
   *   senderAddress: {...},
   *   receiverAddress: {...}
   * }
   *
   * The backend generates tracking number, stores the delivery, and publishes
   * the first tracking event to RabbitMQ.
   */
  return request('/deliveries/deliveries', token, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
};

export const getAdminDashboard = async (token) => {
  /*
   * Admin dashboard data is protected. Even if a normal user tries to call
   * this function manually, the backend should reject it unless the JWT role
   * is ADMIN.
   */
  return request('/admin/admin/dashboard', token, {
    method: 'GET',
  });
};

export const getAdminDeliveries = async (token) => {
  return request('/admin/admin/deliveries', token, {
    method: 'GET',
  });
};

export const updateDeliveryStatus = async (id, status, token) => {
  return request(`/admin/admin/deliveries/${id}/resolve?status=${encodeURIComponent(status)}`, token, {
    method: 'PUT',
  });
};

export const getRevenueTrend = async (token) => {
  /*
   * Fetches monthly revenue aggregates from admin-service.
   * Each entry contains { month: "2026-05", revenue: 12500 }.
   * Used by the Revenue Trends line chart in AdminPage.
   */
  return request('/admin/admin/revenue-trend', token, {
    method: 'GET',
  });
};
