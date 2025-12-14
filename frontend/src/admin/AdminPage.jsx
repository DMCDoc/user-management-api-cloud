// src/admin/AdminPage.jsx
import { useEffect, useState } from "react";
import { Box, Grid, Card, CardContent, Typography } from "@mui/material";
import UsersPage from "./UsersPage";
import { fetchStats } from "./adminApi";
import StatsChart from "./StatsChart";

export default function AdminPage() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    fetchStats().then(setStats).catch(() => setStats({ totalUsers: 0, admins: 0, disabled: 0 }));
  }, []);

  return (
    <Box p={3}>
      <Typography variant="h4" mb={3}>Admin Dashboard</Typography>

      {stats && (
        <Grid container spacing={2} mb={3}>
          <Grid item xs={12} sm={4}>
            <Card><CardContent><Typography variant="h6">Total Users</Typography><Typography variant="h4">{stats.totalUsers}</Typography></CardContent></Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card><CardContent><Typography variant="h6">Admins</Typography><Typography variant="h4">{stats.admins}</Typography></CardContent></Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card><CardContent><Typography variant="h6">Blocked Accounts</Typography><Typography variant="h4">{stats.disabled}</Typography></CardContent></Card>
          </Grid>
        </Grid>
      )}

      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} md={6}>
          {stats && <StatsChart stats={stats} />}
        </Grid>
      </Grid>

      {/* Users page (can be routed separately under /admin/users) */}
      <UsersPage />
    </Box>
  );
}
