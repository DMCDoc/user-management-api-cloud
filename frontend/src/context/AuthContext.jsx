import { createContext, useContext, useState } from "react";
import api from "../api/api";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(localStorage.getItem("token") ? {} : null);

  const login = async (email, password) => {
  try {
    const res = await api.post("/api/auth/login", { email, password });
    
    // ✅ AJOUTEZ des logs pour debugger
    console.log("Login response:", res.data);
    
    // ✅ VÉRIFIEZ la structure de la réponse
    if (res.data.token && res.data.user) {
      localStorage.setItem("token", res.data.token);
      setUser(res.data.user);
    } else {
      throw new Error("Invalid response format");
    }
  } catch (error) {
    console.error("Login error:", error.response?.data || error.message);
    throw error;
  }
};

  const register = async (data) => {
    try {
      // ✅ CORRECTION : Ajoutez /api devant les endpoints
      await api.post("/api/auth/register", data);
    } catch (error) {
      console.error("Register error:", error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);