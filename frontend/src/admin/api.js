const API_URL = "/api/admin";

const getToken = () => localStorage.getItem("token");

export async function fetchUsers() {
  const res = await fetch(`${API_URL}/users`, {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error("Failed to fetch users");
  return res.json();
}

export async function addRole(userId, role) {
  const res = await fetch(`${API_URL}/users/${userId}/roles/${role}`, {
    method: "POST",
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error("Failed to add role");
}

export async function removeRole(userId, role) {
  const res = await fetch(`${API_URL}/users/${userId}/roles/${role}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error("Failed to remove role");
}
