
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();

  // token persistence check: we keep accessToken in user or in localStorage
  const token = user?.accessToken || localStorage.getItem("accessToken");
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
