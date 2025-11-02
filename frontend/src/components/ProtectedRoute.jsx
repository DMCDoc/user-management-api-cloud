import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx"; // ✅ CORRECTION : Ajout de l'extension .jsx

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();
  
  // Vérifie l'état React ET le localStorage pour la persistance
  if (!user && !localStorage.getItem("accessToken")) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
}
