import { Box, Drawer, List, ListItemButton, ListItemIcon, ListItemText, AppBar, Toolbar, IconButton, Typography, Switch } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import PeopleIcon from "@mui/icons-material/People";
import BarChartIcon from "@mui/icons-material/BarChart";
import HistoryIcon from "@mui/icons-material/History";
import { Link } from "react-router-dom";
import { useThemeMode } from "../theme/ThemeProvider";

export default function AdminLayout({ children }) {
  const { mode, toggleMode } = useThemeMode();
  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar position="fixed" sx={{ zIndex: 1300 }}>
        <Toolbar>
          <IconButton edge="start" color="inherit" sx={{ mr: 2 }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Admin</Typography>
          <Switch checked={mode === "dark"} onChange={toggleMode} />
        </Toolbar>
      </AppBar>

      <Drawer variant="permanent" sx={{ width: 220, "& .MuiDrawer-paper": { width: 220, boxSizing: "border-box", top: "64px" } }}>
        <List>
          <ListItemButton component={Link} to="/admin/users">
            <ListItemIcon><PeopleIcon /></ListItemIcon>
            <ListItemText primary="Users" />
          </ListItemButton>
          <ListItemButton component={Link} to="/admin/stats">
            <ListItemIcon><BarChartIcon /></ListItemIcon>
            <ListItemText primary="Stats" />
          </ListItemButton>
          <ListItemButton component={Link} to="/admin/logs">
            <ListItemIcon><HistoryIcon /></ListItemIcon>
            <ListItemText primary="Audit Logs" />
          </ListItemButton>
        </List>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, p: 3, mt: "80px" }}>
        {children}
      </Box>
    </Box>
  );
}
