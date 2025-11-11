import { Navigate } from "react-router-dom";
import jwtDecode from "jwt-decode";

function isAdmin() {
  const token = localStorage.getItem("token");
  if (!token) return false;
  const decoded = jwtDecode(token);
  const roles = decoded.roles || [];
  return roles.includes("ROLE_ADMIN");
}

export function AdminRoute({ children }) {
  return isAdmin() ? children : <Navigate to="/login" />;
}
