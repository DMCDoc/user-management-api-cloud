import React, { useEffect, useState } from "react";
import { fetchUsers, addRole, removeRole } from "./api";
import UserTable from "./UserTable";

export default function AdminPage() {
  const [users, setUsers] = useState([]);

  const loadUsers = async () => {
    try {
      const data = await fetchUsers();
      setUsers(data);
    } catch (err) {
      console.error(err);
      alert("Erreur chargement utilisateurs");
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleAdd = async (id, role) => {
    await addRole(id, role);
    loadUsers();
  };

  const handleRemove = async (id, role) => {
    await removeRole(id, role);
    loadUsers();
  };

  return (
    <div>
      <h1>Panneau Admin</h1>
      <UserTable users={users} onAddRole={handleAdd} onRemoveRole={handleRemove} />
    </div>
  );
}
