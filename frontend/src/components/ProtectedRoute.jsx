import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user && !localStorage.getItem("token")) {
    return <Navigate to="/login" />;
  }
  return children;
}
