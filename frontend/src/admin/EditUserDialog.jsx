// src/admin/EditUserDialog.jsx
import { useEffect, useState } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, FormControlLabel, Checkbox } from "@mui/material";
import { fetchRoles, patchUser } from "./Adminapi";

export default function EditUserDialog({ user, onClose, onSaved }) {
  const [email, setEmail] = useState("");
  const [enabled, setEnabled] = useState(true);
  const [roles, setRoles] = useState([]);
  const [availableRoles, setAvailableRoles] = useState([]);

  useEffect(()=> {
    if(user){ setEmail(user.email || ""); setEnabled(user.enabled ?? true); setRoles(Array.isArray(user.roles) ? user.roles : []); }
    fetchRoles().then(r => setAvailableRoles(Array.isArray(r) ? r : []));
  }, [user]);

  const toggleRole = (r) => setRoles(prev => prev.includes(r) ? prev.filter(x=>x!==r) : [...prev, r]);

  async function save(){
    await patchUser(user.id, { email, enabled, roles });
    onSaved();
    onClose();
  }

  return (
    <Dialog open={!!user} onClose={onClose}>
      <DialogTitle>Edit User</DialogTitle>
      <DialogContent sx={{ pt: 2 }}>
        <TextField fullWidth margin="normal" label="Email" value={email} onChange={(e)=>setEmail(e.target.value)} />
        <FormControlLabel control={<Checkbox checked={enabled} onChange={(e)=>setEnabled(e.target.checked)} />} label="Enabled" />
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginTop: 12 }}>
          {availableRoles.map(r => (
            <Button key={r.name} variant={roles.includes(r.name) ? "contained" : "outlined"} onClick={()=>toggleRole(r.name)}>
              {r.name}
            </Button>
          ))}
        </div>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}
