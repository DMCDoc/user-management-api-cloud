
import { useAuth } from "../context/AuthContext";
import { Navigate } from "react-router-dom";

export default function AdminRoute({ children }) {
  const { user } = useAuth();

  if (!user || !user.accessToken) {
    return <Navigate to="/login" replace />;
  }

  // backend should return roles as ["ROLE_ADMIN", ...]
  if (!user.roles || !user.roles.includes("ROLE_ADMIN")) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
