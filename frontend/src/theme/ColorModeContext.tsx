import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { CssBaseline, ThemeProvider, type PaletteMode } from "@mui/material";
import { getAppTheme } from "./theme";

const STORAGE_KEY = "pf_color_mode";

interface ColorModeContextValue {
  mode: PaletteMode;
  toggleMode: () => void;
  setMode: (mode: PaletteMode) => void;
}

const ColorModeContext = createContext<ColorModeContextValue | null>(null);

function getInitialMode(): PaletteMode {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === "light" || stored === "dark") {
    return stored;
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

export function ColorModeProvider({ children }: { children: ReactNode }) {
  const [mode, setModeState] = useState<PaletteMode>(getInitialMode);

  const setMode = (next: PaletteMode) => {
    localStorage.setItem(STORAGE_KEY, next);
    setModeState(next);
  };

  const value = useMemo<ColorModeContextValue>(
    () => ({
      mode,
      toggleMode: () => setMode(mode === "light" ? "dark" : "light"),
      setMode,
    }),
    [mode]
  );

  const theme = useMemo(() => getAppTheme(mode), [mode]);

  return (
    <ColorModeContext.Provider value={value}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
}

export function useColorMode(): ColorModeContextValue {
  const ctx = useContext(ColorModeContext);
  if (!ctx) {
    throw new Error("useColorMode must be used within a ColorModeProvider");
  }
  return ctx;
}
