import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();
  
  // ✅ CORRECTION : Logique plus claire
  if (!user && !localStorage.getItem("token")) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
}