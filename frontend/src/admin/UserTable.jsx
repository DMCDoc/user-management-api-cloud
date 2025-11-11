import React from "react";

export default function UserTable({ users, onAddRole, onRemoveRole }) {
  return (
    <table>
      <thead>
        <tr>
          <th>Email</th>
          <th>Username</th>
          <th>Roles</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {users.map((u) => (
          <tr key={u.id}>
            <td>{u.email}</td>
            <td>{u.username}</td>
            <td>{u.roles.map(r => r.name).join(", ")}</td>
            <td>
              <button onClick={() => onAddRole(u.id, "ROLE_ADMIN")}>+ Admin</button>
              <button onClick={() => onRemoveRole(u.id, "ROLE_ADMIN")}>− Admin</button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
