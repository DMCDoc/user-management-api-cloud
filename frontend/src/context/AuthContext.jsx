
import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/api";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem("accessToken");
    const profile = localStorage.getItem("profile");
    return token ? { accessToken: token, ...(profile ? JSON.parse(profile) : {}) } : null;
  });

  // keep profile in sync when localStorage changes in other tabs
  useEffect(() => {
    const onStorage = () => {
      const token = localStorage.getItem("accessToken");
      const profile = localStorage.getItem("profile");
      setUser(token ? { accessToken: token, ...(profile ? JSON.parse(profile) : {}) } : null);
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const loadProfile = async () => {
    try {
      const res = await api.get("/api/auth/me");
      const profile = res.data;
      // profile should include roles, id, username, email, etc.
      const accessToken = localStorage.getItem("accessToken");
      localStorage.setItem("profile", JSON.stringify(profile));
      setUser({ accessToken, ...profile });
      return profile;
    } catch (e) {
      // if profile fetch fails, logout
      logout();
      throw e;
    }
  };

  const login = async (email, password) => {
    try {
      const res = await api.post("/api/auth/login", { email, password });
      const data = res.data;
      if (!data || !data.accessToken) throw new Error("Invalid login response");

      localStorage.setItem("accessToken", data.accessToken);
      // if backend returns user profile in login response:
      if (data.user) {
        localStorage.setItem("profile", JSON.stringify(data.user));
        setUser({ accessToken: data.accessToken, ...data.user });
      } else {
        // otherwise load profile
        await loadProfile();
      }
      return true;
    } catch (error) {
      console.error("Login error:", error.response?.data || error.message);
      throw error;
    }
  };

  const register = async (payload) => {
    try {
      await api.post("/api/auth/register", payload);
      return true;
    } catch (err) {
      console.error("Register error:", err.response?.data || err.message);
      throw err;
    }
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("profile");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, register, loadProfile }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
