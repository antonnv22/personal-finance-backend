import {
  Avatar,
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
  Typography,
} from "@mui/material";
import { NavLink, useLocation } from "react-router-dom";
import WalletRoundedIcon from "@mui/icons-material/WalletRounded";
import { NAV_ITEMS } from "./navItems";

export const DRAWER_WIDTH = 264;

interface SidebarProps {
  mobileOpen: boolean;
  onClose: () => void;
}

function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const location = useLocation();

  return (
    <Box sx={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <Stack direction="row" spacing={1.5} alignItems="center" sx={{ px: 3, py: 3 }}>
        <Avatar sx={{ bgcolor: "primary.main", width: 40, height: 40 }}>
          <WalletRoundedIcon fontSize="small" />
        </Avatar>
        <Box>
          <Typography variant="subtitle1" lineHeight={1.2}>
            Personal Finance
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Управление бюджетом
          </Typography>
        </Box>
      </Stack>

      <Divider />

      <List sx={{ px: 1.5, py: 2, flexGrow: 1 }}>
        {NAV_ITEMS.map((item) => {
          const selected =
            item.path === "/" ? location.pathname === "/" : location.pathname.startsWith(item.path);
          const Icon = item.icon;
          return (
            <ListItemButton
              key={item.path}
              component={NavLink}
              to={item.path}
              onClick={onNavigate}
              selected={selected}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                "&.Mui-selected": {
                  bgcolor: "primary.main",
                  color: "primary.contrastText",
                  "& .MuiListItemIcon-root": { color: "primary.contrastText" },
                  "&:hover": { bgcolor: "primary.dark" },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>
                <Icon fontSize="small" />
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }}
              />
            </ListItemButton>
          );
        })}
      </List>

      <Box sx={{ px: 3, py: 2 }}>
        <Typography variant="caption" color="text.secondary">
          © {new Date().getFullYear()} Personal Finance
        </Typography>
      </Box>
    </Box>
  );
}

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  return (
    <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: "block", md: "none" },
          "& .MuiDrawer-paper": { boxSizing: "border-box", width: DRAWER_WIDTH },
        }}
      >
        <SidebarContent onNavigate={onClose} />
      </Drawer>
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: "none", md: "block" },
          "& .MuiDrawer-paper": {
            boxSizing: "border-box",
            width: DRAWER_WIDTH,
            borderRight: "1px solid",
            borderColor: "divider",
          },
        }}
        open
      >
        <SidebarContent />
      </Drawer>
    </Box>
  );
}
