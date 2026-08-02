import { createTheme, type PaletteMode, type ThemeOptions } from "@mui/material/styles";

const brand = {
  main: "#1F6FEB",
  light: "#5B9AFF",
  dark: "#0D4FBE",
};

function getDesignTokens(mode: PaletteMode): ThemeOptions {
  const isLight = mode === "light";
  return {
    palette: {
      mode,
      primary: {
        main: brand.main,
        light: brand.light,
        dark: brand.dark,
        contrastText: "#ffffff",
      },
      secondary: {
        main: "#00B894",
      },
      success: { main: "#1DB870" },
      error: { main: "#E5484D" },
      warning: { main: "#F5A623" },
      background: {
        default: isLight ? "#F4F6FB" : "#0B1220",
        paper: isLight ? "#FFFFFF" : "#131C2E",
      },
      divider: isLight ? "rgba(15, 23, 42, 0.08)" : "rgba(255, 255, 255, 0.08)",
    },
    shape: {
      borderRadius: 14,
    },
    typography: {
      fontFamily: [
        "Inter",
        "-apple-system",
        "BlinkMacSystemFont",
        '"Segoe UI"',
        "Roboto",
        '"Helvetica Neue"',
        "Arial",
        "sans-serif",
      ].join(","),
      h4: { fontWeight: 700 },
      h5: { fontWeight: 700 },
      h6: { fontWeight: 600 },
      subtitle1: { fontWeight: 600 },
      button: { fontWeight: 600, textTransform: "none" as const },
    },
    components: {
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: "none",
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            border: `1px solid ${isLight ? "rgba(15, 23, 42, 0.06)" : "rgba(255, 255, 255, 0.06)"}`,
            boxShadow: isLight
              ? "0 1px 2px rgba(15, 23, 42, 0.04), 0 8px 24px rgba(15, 23, 42, 0.04)"
              : "0 1px 2px rgba(0, 0, 0, 0.2), 0 8px 24px rgba(0, 0, 0, 0.24)",
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
          },
        },
      },
      MuiTextField: {
        defaultProps: {
          size: "small",
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            fontWeight: 600,
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            boxShadow: "none",
            borderBottom: `1px solid ${isLight ? "rgba(15, 23, 42, 0.06)" : "rgba(255, 255, 255, 0.06)"}`,
          },
        },
      },
    },
  };
}

export function getAppTheme(mode: PaletteMode) {
  return createTheme(getDesignTokens(mode));
}
