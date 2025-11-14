import api from "./Adminapi";

export async function fetchUsers() {
  const response = await api.get("/admin/users");
  return response.data;
}

export async function updateUserRole(id, roleName) {
  const response = await api.put(`/admin/users/${id}/role`, null, {
    params: { roleName }
  });
  return response.data;
}

export async function deleteUser(id) {
  return api.delete(`/admin/users/${id}`);
}
