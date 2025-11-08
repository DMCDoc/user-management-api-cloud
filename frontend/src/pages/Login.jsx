import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");// ✅ AJOUTEZ
  const [msg, setMsg] = useState(null);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); // ✅ Réinitialiser les erreurs
    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      setError("Login failed. Please check your credentials."); // ✅ Gestion d'erreur
    }
  
  };

  return (
    <div>
      <h1>Login</h1>
      {error && <p style={{ color: "red" }}>{error}</p>} {/* ✅ Afficher l'erreur */}
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
         <div>
      <h2>Mot de passe oublié</h2>
      <form onSubmit={handleSubmit}>
        <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email" required/>
        <button>Envoyer</button>
      </form>
      {msg && <p>{msg}</p>}
    </div>
    </div>
    
  );
}