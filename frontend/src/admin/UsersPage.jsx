import { useEffect, useState } from "react";
import AdminLayout from "./AdminLayout";
import { Box, Button, TextField } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import EditUserDialog from "./EditUserDialog";
import { fetchUsers, deleteUser, adminResetPassword, blockUser, unblockUser } from "./api";

export default function UsersPage(){
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [search, setSearch] = useState("");
  const [rows, setRows] = useState([]);
  const [pageInfo, setPageInfo] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [editUser, setEditUser] = useState(null);

  const load = async (p = page) => {
    setLoading(true);
    try {
      const data = await fetchUsers(p, size, search);
      setRows(data.content || []);
      setPageInfo({ totalPages: data.totalPages || 0, totalElements: data.totalElements || 0 });
    } finally {
      setLoading(false);
    }
  };

  useEffect(()=> { load(0); }, []); // load first page

  useEffect(()=> { load(page); }, [page]);

  const columns = [
    { field: "id", headerName: "ID", width: 220 },
    { field: "email", headerName: "Email", flex: 1 },
    { field: "username", headerName: "Username", width: 180 },
    { field: "roles", headerName: "Roles", width: 220, valueGetter: (p)=> Array.isArray(p.row.roles) ? p.row.roles.join(", ") : "" },
    { field: "enabled", headerName: "Enabled", width: 120, type: "boolean" },
    {
      field: "actions",
      headerName: "Actions",
      width: 420,
      renderCell: (params) => (
        <>
          <Button size="small" onClick={()=> setEditUser(params.row)}>Edit</Button>
          <Button size="small" color="error" onClick={()=> deleteUser(params.row.id).then(()=>load(page))}>Delete</Button>
          <Button size="small" onClick={async ()=> { const r = await adminResetPassword(params.row.id); alert("New password: " + (r.tempPassword || r.newPassword || r)); }}>Reset PW</Button>
          {params.row.enabled ? (
            <Button size="small" color="warning" onClick={()=> blockUser(params.row.id).then(()=>load(page))}>Block</Button>
          ) : (
            <Button size="small" color="success" onClick={()=> unblockUser(params.row.id).then(()=>load(page))}>Unblock</Button>
          )}
        </>
      )
    }
  ];

  return (
    <AdminLayout>
      <Box mb={2} display="flex" gap={2}>
        <TextField label="Search" size="small" value={search} onChange={(e)=>setSearch(e.target.value)} onKeyDown={(e)=> e.key==="Enter" && (setPage(0), load(0))}/>
        <Button variant="contained" onClick={()=> { setPage(0); load(0); }}>Search</Button>
      </Box>

      <div style={{ height: 600 }}>
        <DataGrid
          rows={rows}
          columns={columns}
          pageSizeOptions={[size]}
          paginationMode="server"
          rowCount={pageInfo.totalElements}
          paginationModel={{ page, pageSize: size }}
          onPaginationModelChange={(m) => setPage(m.page)}
          loading={loading}
          getRowId={(row) => row.id}
        />
      </div>

      {editUser && <EditUserDialog user={editUser} onClose={() => setEditUser(null)} onSaved={() => { setEditUser(null); load(page); }} />}
    </AdminLayout>
  );
}
