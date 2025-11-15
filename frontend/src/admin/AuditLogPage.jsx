import { useEffect, useState } from "react";
import AdminLayout from "./AdminLayout";
import { DataGrid } from "@mui/x-data-grid";
import { fetchLogs } from "./api";

export default function AuditLogPage(){
  const [rows, setRows] = useState([]);
  useEffect(()=> { fetchLogs().then(setRows).catch(()=>setRows([])); }, []);
  const columns = [
    { field: "timestamp", headerName: "Date", flex: 1 },
    { field: "adminEmail", headerName: "Admin", flex: 1 },
    { field: "action", headerName: "Action", flex: 1 },
    { field: "userId", headerName: "User ID", width: 220 }
  ];
  return <AdminLayout><div style={{height:600}}><DataGrid rows={rows} columns={columns} getRowId={(r)=> r.id} /></div></AdminLayout>;
}
