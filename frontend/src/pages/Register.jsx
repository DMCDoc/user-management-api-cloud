import { useState } from "react";
import { useAuth } from "../context/AuthContext.jsx"; // ✅ CORRECTION : Ajout de l'extension .jsx
import { useNavigate } from "react-router-dom";

export default function Register() {
  const { register } = useAuth();
  
  // Inclure le 'username' dans l'état
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.username || !form.email || !form.password) {
      setError("Tous les champs sont requis.");
      return;
    }
    try {
      await register(form);
      navigate("/login");
    } catch (err) {
      setError("L'inscription a échoué. L'email ou le nom d'utilisateur est peut-être déjà pris.");
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 p-4">
      <div className="w-full max-w-md bg-white p-8 rounded-xl shadow-2xl">
        <h2 className="text-3xl font-extrabold text-gray-900 mb-6 text-center">Créer un Compte</h2>
        {error && <p className="text-red-600 bg-red-100 p-3 rounded-lg mb-4 text-sm text-center font-medium">{error}</p>}
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <input 
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
            placeholder="Nom d'utilisateur"
            name="username"
            value={form.username} 
            onChange={handleChange}
            required
          />
          <input 
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
            placeholder="Email"
            name="email"
            type="email"
            value={form.email} 
            onChange={handleChange}
            required
          />
          <input
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
            placeholder="Mot de passe"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
          />
          <button 
            type="submit"
            className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold text-lg hover:bg-blue-700 transition duration-150 ease-in-out shadow-md hover:shadow-lg"
          >
            S'inscrire
          </button>
        </form>
        
        <p className="mt-6 text-center text-sm text-gray-600">
          Déjà un compte ? 
          <a onClick={() => navigate("/login")} className="font-medium text-blue-600 hover:text-blue-500 cursor-pointer ml-1">
            Connectez-vous ici
          </a>
        </p>
      </div>
    </div>
  );
}
