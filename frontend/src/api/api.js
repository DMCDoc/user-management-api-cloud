import axios from 'axios';

// Utiliser une base URL vide car Nginx gère le routage entre le front et le back
const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour ajouter le token à chaque requête authentifiée
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken'); // Utiliser accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
