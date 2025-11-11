import { useState, useEffect } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [password, setPassword] = useState("");
  const [msg, setMsg] = useState(null);
  const navigate = useNavigate();

  useEffect(()=> {
    if(!token) setMsg("Token manquant.");
  }, [token]);

  async function handleSubmit(e) {
    e.preventDefault();
    const res = await fetch("/api/auth/reset-password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token, password })
    });
    const data = await res.json();
    if(res.ok) {
      setMsg(data.message);
      setTimeout(()=>navigate("/login"), 1500);
    } else {
      setMsg(data.error || "Erreur");
    }
  }

  return (
    <div>
      <h2>Réinitialiser le mot de passe</h2>
      <form onSubmit={handleSubmit}>
        <input value={password} onChange={e=>setPassword(e.target.value)} type="password" placeholder="Nouveau mot de passe" required minLength={8}/>
        <button>Valider</button>
      </form>
      {msg && <p>{msg}</p>}
    </div>
  );
}
