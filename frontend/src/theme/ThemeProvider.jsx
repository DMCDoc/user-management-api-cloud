import { createTheme, ThemeProvider as MUIThemeProvider } from "@mui/material/styles";
import { useMemo, createContext, useContext, useState } from "react";

const ThemeModeContext = createContext();
export const useThemeMode = () => useContext(ThemeModeContext);

export function ThemeProvider({ children }) {
  const [mode, setMode] = useState(() => localStorage.getItem("theme") || "light");
  const toggleMode = () => { const m = mode === "light" ? "dark" : "light"; setMode(m); localStorage.setItem("theme", m); };

  const theme = useMemo(() => createTheme({ palette: { mode } }), [mode]);

  return (
    <ThemeModeContext.Provider value={{ mode, toggleMode }}>
      <MUIThemeProvider theme={theme}>{children}</MUIThemeProvider>
    </ThemeModeContext.Provider>
  );
}
