import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";

export default function RoleSelect({ value, onChange }) {
  const roles = ["USER", "ADMIN", "MODERATOR"];

  return (
    <FormControl fullWidth margin="normal">
      <InputLabel>Roles</InputLabel>
      <Select
        multiple
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {roles.map((role) => (
          <MenuItem key={role} value={role}>
            {role}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
}
