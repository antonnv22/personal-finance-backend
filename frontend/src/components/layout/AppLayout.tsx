import { useState } from "react";
import { Box, Container } from "@mui/material";
import { Outlet, useLocation } from "react-router-dom";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";
import { NAV_ITEMS } from "./navItems";

function useCurrentTitle(): string {
  const location = useLocation();
  const match = [...NAV_ITEMS]
    .sort((a, b) => b.path.length - a.path.length)
    .find((item) => (item.path === "/" ? location.pathname === "/" : location.pathname.startsWith(item.path)));
  return match?.label ?? "Personal Finance";
}

export function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const title = useCurrentTitle();

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <Sidebar mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} />
      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column", minWidth: 0 }}>
        <Topbar title={title} onMenuClick={() => setMobileOpen(true)} />
        <Box component="main" sx={{ flexGrow: 1, py: { xs: 2.5, md: 4 } }}>
          <Container maxWidth="xl">
            <Outlet />
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
