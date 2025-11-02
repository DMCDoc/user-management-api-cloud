import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/api.js"; // ✅ CORRECTION : Utilisation du chemin explicite avec l'extension .js

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  // Initialiser l'état user en lisant le token ET l'email du localStorage
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem("accessToken");
    const email = localStorage.getItem("userEmail");
    return token ? { email: email } : null;
  });

  // Gérer les changements de stockage externe (déconnexion dans un autre onglet)
  useEffect(() => {
    const handleStorageChange = () => {
      const token = localStorage.getItem("accessToken");
      const email = localStorage.getItem("userEmail");
      setUser(token ? { email: email } : null);
    };

    window.addEventListener('storage', handleStorageChange);
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const login = async (email, password) => {
    try {
      // Les requêtes passent par Nginx, qui transfère vers le backend Spring Boot
      const res = await api.post("/api/auth/login", { email, password });
      
      if (res.data.accessToken && res.data.email) {
        localStorage.setItem("accessToken", res.data.accessToken);
        localStorage.setItem("userEmail", res.data.email);
        setUser({ email: res.data.email });
      } else {
        throw new Error("Format de réponse invalide depuis l'API");
      }
    } catch (error) {
      console.error("Login error:", error.response?.data || error.message);
      throw error;
    }
  };

  const register = async (data) => {
    try {
      // data doit contenir { username, email, password }
      await api.post("/api/auth/register", data);
    } catch (error) {
      console.error("Register error:", error.response?.data?.message || error.message);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userEmail");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
