
import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "", // empty means relative -> nginx proxy
  headers: {
    "Content-Type": "application/json",
  },
});

// attach token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (err) => Promise.reject(err));

// optional response interceptor to handle 401 + refresh (not implemented here)
// add refresh logic if you have refresh token endpoint

export default api;
