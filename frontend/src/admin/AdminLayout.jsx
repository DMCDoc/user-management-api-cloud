export default function AdminLayout({ children }) {
  return (
    <div style={{ display: "flex", height: "100vh", background: "#f4f4f4" }}>
      <aside
        style={{
          width: "220px",
          background: "#222",
          color: "white",
          padding: "20px",
        }}
      >
        <h2 style={{ marginBottom: "20px" }}>Admin</h2>
        <ul style={{ listStyle: "none", padding: 0 }}>
          <li><a href="/admin" style={{ color: "white" }}>Dashboard</a></li>
          <li><a href="/dashboard" style={{ color: "white" }}>Retour App</a></li>
        </ul>
      </aside>

      <main style={{ flex: 1, padding: "20px" }}>{children}</main>
    </div>
  );
}
