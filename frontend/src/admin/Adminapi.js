import api from "../api/api";

export async function fetchUsers(page = 0, size = 20, search = "") {
  const res = await api.get("/api/admin/users", { params: { page, size, search } });
  return res.data; // expect Page<UserDto> with content, totalPages, totalElements, number
}

export async function fetchStats() {
  const res = await api.get("/api/admin/stats");
  return res.data;
}

export async function fetchLogs() {
  const res = await api.get("/api/admin/logs");
  return res.data;
}

export async function fetchRoles() {
  const res = await api.get("/api/admin/roles");
  return res.data;
}

export async function patchUser(userId, body) {
  const res = await api.patch(`/api/admin/users/${userId}`, body);
  return res.data;
}

export async function updateUserRoles(userId, roles) {
  // roles: array of role names
  const res = await api.put(`/api/admin/users/${userId}/roles`, roles);
  return res.data;
}

export async function deleteUser(userId) {
  return api.delete(`/api/admin/users/${userId}`);
}

export async function blockUser(userId) {
  return api.put(`/api/admin/users/${userId}/block`);
}

export async function unblockUser(userId) {
  return api.put(`/api/admin/users/${userId}/unblock`);
}

export async function adminResetPassword(userId) {
  const res = await api.post(`/api/admin/users/${userId}/reset-password`);
  return res.data;
}
