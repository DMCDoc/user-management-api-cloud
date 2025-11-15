import { useEffect, useState } from "react";
import { Box, TextField, IconButton } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import LockIcon from "@mui/icons-material/Lock";
import LockOpenIcon from "@mui/icons-material/LockOpen";

import { fetchUsers, deleteUser } from "./api";
import { adminResetPassword } from "./api";
import EditUserDialog from "./EditUserDialog";

export default function UserTable() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [openEdit, setOpenEdit] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  async function loadUsers() {
    const data = await fetchUsers(page, 20, search);
    setUsers(data.content);
  }

  useEffect(() => {
    loadUsers();
  }, [page, search]);

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "email", headerName: "Email", flex: 1 },
    { field: "roles", headerName: "Roles", width: 200 },
    { field: "blocked", headerName: "Blocked", width: 120, type: "boolean" },
    {
      field: "actions",
      headerName: "Actions",
      width: 160,
      renderCell: (params) => (
        <>
          <IconButton
            color="primary"
            onClick={() => {
              setSelectedUser(params.row);
              setOpenEdit(true);
            }}>
            <EditIcon />
          </IconButton>

          <IconButton
            color="error"
            onClick={() => deleteUser(params.row.id).then(loadUsers)}>
            <DeleteIcon />
          </IconButton>
        </>
      ),
    },
  ];

  return (
    <Box>
      <TextField
        label="Recherche utilisateur"
        variant="outlined"
        size="small"
        fullWidth
        sx={{ mb: 2 }}
        onChange={(e) => setSearch(e.target.value)}
      />

      <div style={{ height: 500 }}>
        <DataGrid
          rows={users}
          columns={columns}
          pageSizeOptions={[20]}
          paginationModel={{ page, pageSize: 20 }}
          onPaginationModelChange={(p) => setPage(p.page)}
        />
      </div>

      <EditUserDialog
        open={openEdit}
        onClose={() => setOpenEdit(false)}
        user={selectedUser}
        onUpdated={loadUsers}
      />
    </Box>
  );
}

{
  field: "block",
    headerName; "Block",
    width; 100,
    renderCell; (params) => (
      params.row.blocked ? (
        <IconButton color="warning" onClick={() => unblockUser(params.row.id).then(loadUsers)}>
          <LockOpenIcon />
        </IconButton>
      ) : (
        <IconButton color="warning" onClick={() => blockUser(params.row.id).then(loadUsers)}>
          <LockIcon />
        </IconButton>
      )
    );
}

{
  field: "reset",
    headerName; "Reset PW",
    width; 120,
    renderCell; (params) => (
    <IconButton
      color="secondary"
      onClick={async () => {
        const data = await adminResetPassword(params.row.id);
        alert("New password: " + data.newPassword);
      }}
    >
      <RestartAltIcon />
    </IconButton>
  )
}
