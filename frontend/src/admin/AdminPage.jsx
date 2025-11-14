import { useEffect, useState } from "react";
import AdminLayout from "./AdminLayout";
import UserTable from "./UserTable";
import { fetchUsers } from "./Adminapi";

export default function AdminPage() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    fetchUsers().then(setUsers);
  }, []);

  return (
    <AdminLayout>
      <h1>Gestion des utilisateurs</h1>
      <UserTable users={users} />
    </AdminLayout>
  );
}
