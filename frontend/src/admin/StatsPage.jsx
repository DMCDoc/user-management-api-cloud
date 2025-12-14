import { useEffect, useState } from "react";
import AdminLayout from "./AdminLayout";
import { Card, CardContent, Typography, Grid } from "@mui/material";
import { fetchStats } from "./adminApi";
import StatsChart from "./StatsChart";

export default function StatsPage(){
  const [stats, setStats] = useState(null);
  useEffect(()=> { fetchStats().then(setStats).catch(()=>setStats({ totalUsers:0, admins:0, disabled:0 })); }, []);
  if(!stats) return <AdminLayout><div>Loading...</div></AdminLayout>;
  return (
    <AdminLayout>
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}><Card><CardContent><Typography>Total Users</Typography><Typography variant="h4">{stats.totalUsers}</Typography></CardContent></Card></Grid>
        <Grid item xs={12} md={6}><StatsChart stats={stats} /></Grid>
      </Grid>
    </AdminLayout>
  );
}
