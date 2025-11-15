import { useState } from "react";//peut etr à supprimer

export default function EditUserModal({ user, roles, onClose, onSave }) {
  const [email, setEmail] = useState(user.email);
  const [username, setUsername] = useState(user.username);
  const [enabled, setEnabled] = useState(user.enabled);
  const [sel, setSel] = useState(user.roles);

  function toggleRole(r){
    setSel(prev => prev.includes(r) ? prev.filter(x=>x!==r) : [...prev, r]);
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50">
      <div className="bg-white p-6 rounded w-96">
        <h3 className="mb-4">Edit user</h3>
        <label>Email<input className="w-full" value={email} onChange={e=>setEmail(e.target.value)} /></label>
        <label>Username<input className="w-full" value={username} onChange={e=>setUsername(e.target.value)} /></label>
        <label className="flex items-center gap-2 mt-2">
          <input type="checkbox" checked={enabled} onChange={e=>setEnabled(e.target.checked)} /> Enabled
        </label>
        <div className="mt-3">
          <div className="text-sm text-gray-600 mb-2">Roles</div>
          <div className="flex gap-2 flex-wrap">
            {roles.map(r => (
              <button key={r.name} type="button" onClick={()=>toggleRole(r.name)}
                className={`px-2 py-1 border rounded ${sel.includes(r.name) ? "bg-blue-600 text-white" : ""}`}>
                {r.name}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={onClose} className="px-3 py-1">Cancel</button>
          <button onClick={()=> onSave({ email, username, enabled, roles: new Set(sel) })} className="px-3 py-1 bg-blue-600 text-white">Save</button>
        </div>
      </div>
    </div>
  );
}
