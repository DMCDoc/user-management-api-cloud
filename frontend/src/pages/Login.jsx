import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [msg, setMsg] = useState(null);
  const navigate = useNavigate();

  // NOUVEAU: Un état séparé pour l'email de réinitialisation
  const [resetEmail, setResetEmail] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMsg(null); // Vider les messages de l'autre formulaire
    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      setError("Login failed. Please check your credentials.");
    }
  };

  // NOUVEAU: Une fonction séparée pour la demande de réinitialisation
  const handlePasswordReset = async (e) => {
    e.preventDefault();
    setError("");
    setMsg(null);

    // Mettez ici l'URL de votre API backend
    const API_URL = "/api/auth/forgot-password";

    try {
      const res = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        // MODIFIÉ: Utiliser l'état 'resetEmail'
        body: JSON.stringify({ email: resetEmail }), 
      });

      const data = await res.json();

      if (res.ok) {
        setMsg("Si un compte existe pour cet email, un lien de réinitialisation a été envoyé.");
        setResetEmail(""); // Vider le champ après succès
      } else {
        setError(data.error || "Une erreur est survenue.");
      }
    } catch (err) {
      setError("Erreur de connexion au serveur.");
    }
  };

  return (
    <div>
      <h1>Login</h1>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit">Login</button>
      </form>
      
      <hr /> {/* Séparateur visuel */}

      <div>
        <h2>Mot de passe oublié</h2>
        {/* MODIFIÉ: Appeler la nouvelle fonction handlePasswordReset */}
        <form onSubmit={handlePasswordReset}> 
          <input
            // MODIFIÉ: Utiliser le nouvel état 'resetEmail'
            value={resetEmail} 
            onChange={(e) => setResetEmail(e.target.value)} 
            placeholder="Email"
            type="email" // Bonne pratique
            required
          />
          <button type="submit">Envoyer</button>
        </form>
        {/* 'msg' est maintenant utilisé pour le succès de la réinitialisation */}
        {msg && <p style={{ color: "green" }}>{msg}</p>} 
      </div>
    </div>
  );
}