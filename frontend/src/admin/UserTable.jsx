import { updateUserRole, deleteUser } from "./Adminapi";

export default function UserTable({ users }) {
  async function handleRoleChange(id, role) {
    await updateUserRole(id, role);
    alert("Role mis à jour");
  }

  async function handleDelete(id) {
    if (confirm("Supprimer cet utilisateur ?")) {
      await deleteUser(id);
      alert("Utilisateur supprimé");
      location.reload();
    }
  }

  return (
    <table style={{ width: "100%", background: "white", padding: "10px" }}>
      <thead>
        <tr>
          <th>ID</th><th>Email</th><th>Rôles</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {users.map((u) => (
          <tr key={u.id}>
            <td>{u.id}</td>
            <td>{u.email}</td>
            <td>{u.roles?.join(", ")}</td>
            <td>
              <button onClick={() => handleRoleChange(u.id, "ROLE_ADMIN")}>
                Admin
              </button>
              <button onClick={() => handleRoleChange(u.id, "ROLE_USER")}>
                User
              </button>
              <button onClick={() => handleDelete(u.id)}>
                Supprimer
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
