import { useAuth } from "../context/AuthContext";
import { Navigate } from "react-router-dom";

export function AdminRoute({ children }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (!user.roles || !user.roles.includes("ROLE_ADMIN")) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
