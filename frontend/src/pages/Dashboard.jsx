import { useAuth } from "../context/AuthContext";

export default function Dashboard() {
  const { user, logout } = useAuth();
  return (
    <div>
      <h2>Dashboard</h2>
      <p>Welcome {user?.email || "user"}</p>
      <button onClick={logout}>Logout</button>
    </div>
  );
}
