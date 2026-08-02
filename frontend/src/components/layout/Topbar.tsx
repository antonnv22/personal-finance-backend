import { useState } from "react";
import {
  AppBar,
  Avatar,
  Box,
  IconButton,
  Menu,
  MenuItem,
  Stack,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery,
} from "@mui/material";
import { useTheme } from "@mui/material/styles";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import LightModeRoundedIcon from "@mui/icons-material/LightModeRounded";
import DarkModeRoundedIcon from "@mui/icons-material/DarkModeRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/useAuth";
import { useColorMode } from "@/theme/ColorModeContext";
import { initialsFromEmail } from "@/utils/format";
import { DRAWER_WIDTH } from "./Sidebar";

interface TopbarProps {
  title: string;
  onMenuClick: () => void;
}

export function Topbar({ title, onMenuClick }: TopbarProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const { mode, toggleMode } = useColorMode();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleLogout = () => {
    setAnchorEl(null);
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <AppBar
      position="sticky"
      color="inherit"
      sx={{
        bgcolor: "background.paper",
        width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
        ml: { md: `${DRAWER_WIDTH}px` },
      }}
    >
      <Toolbar sx={{ gap: 1 }}>
        {isMobile && (
          <IconButton edge="start" onClick={onMenuClick} aria-label="Открыть меню">
            <MenuRoundedIcon />
          </IconButton>
        )}
        <Typography variant="h6" sx={{ flexGrow: 1 }} noWrap>
          {title}
        </Typography>

        <Tooltip title={mode === "light" ? "Тёмная тема" : "Светлая тема"}>
          <IconButton onClick={toggleMode} aria-label="Переключить тему">
            {mode === "light" ? <DarkModeRoundedIcon /> : <LightModeRoundedIcon />}
          </IconButton>
        </Tooltip>

        <IconButton onClick={(e) => setAnchorEl(e.currentTarget)} sx={{ ml: 0.5 }}>
          <Avatar sx={{ width: 34, height: 34, bgcolor: "secondary.main", fontSize: 14 }}>
            {user ? initialsFromEmail(user.email) : "?"}
          </Avatar>
        </IconButton>
        <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
          <Box sx={{ px: 2, py: 1 }}>
            <Typography variant="body2" fontWeight={600} noWrap maxWidth={220}>
              {user?.email}
            </Typography>
          </Box>
          <MenuItem
            onClick={() => {
              setAnchorEl(null);
              navigate("/settings");
            }}
          >
            <Stack direction="row" spacing={1.5} alignItems="center">
              <SettingsRoundedIcon fontSize="small" />
              <span>Настройки</span>
            </Stack>
          </MenuItem>
          <MenuItem onClick={handleLogout}>
            <Stack direction="row" spacing={1.5} alignItems="center" color="error.main">
              <LogoutRoundedIcon fontSize="small" />
              <span>Выйти</span>
            </Stack>
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}
