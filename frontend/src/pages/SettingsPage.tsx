import { useState } from "react";
import {
  Avatar,
  Box,
  Card,
  CardContent,
  Divider,
  Stack,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
  Button,
} from "@mui/material";
import LightModeRoundedIcon from "@mui/icons-material/LightModeRounded";
import DarkModeRoundedIcon from "@mui/icons-material/DarkModeRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/useAuth";
import { useColorMode } from "@/theme/ColorModeContext";
import { PageHeader } from "@/components/common/PageHeader";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { initialsFromEmail } from "@/utils/format";

export function SettingsPage() {
  const { user, logout } = useAuth();
  const { mode, setMode } = useColorMode();
  const navigate = useNavigate();
  const [confirmLogout, setConfirmLogout] = useState(false);

  return (
    <Stack spacing={3} maxWidth={640}>
      <PageHeader title="Настройки" subtitle="Профиль и параметры приложения" />

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Профиль
          </Typography>
          <Stack direction="row" spacing={2} alignItems="center" sx={{ mt: 1.5 }}>
            <Avatar sx={{ width: 56, height: 56, bgcolor: "secondary.main" }}>
              {user ? initialsFromEmail(user.email) : "?"}
            </Avatar>
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body1" fontWeight={600} noWrap>
                {user?.email}
              </Typography>
              <Typography variant="caption" color="text.secondary" noWrap>
                ID: {user?.userId}
              </Typography>
            </Box>
          </Stack>
          <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 2 }}>
            Изменение email и пароля пока недоступно в этой версии приложения.
          </Typography>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Внешний вид
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Выберите светлую или тёмную тему интерфейса
          </Typography>
          <ToggleButtonGroup
            value={mode}
            exclusive
            onChange={(_, value) => value && setMode(value)}
          >
            <ToggleButton value="light">
              <LightModeRoundedIcon fontSize="small" sx={{ mr: 1 }} />
              Светлая
            </ToggleButton>
            <ToggleButton value="dark">
              <DarkModeRoundedIcon fontSize="small" sx={{ mr: 1 }} />
              Тёмная
            </ToggleButton>
          </ToggleButtonGroup>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Сессия
          </Typography>
          <Divider sx={{ my: 1.5 }} />
          <Button
            variant="outlined"
            color="error"
            startIcon={<LogoutRoundedIcon />}
            onClick={() => setConfirmLogout(true)}
          >
            Выйти из аккаунта
          </Button>
        </CardContent>
      </Card>

      <ConfirmDialog
        open={confirmLogout}
        title="Выйти из аккаунта?"
        description="Вам потребуется снова войти с помощью email и пароля."
        confirmLabel="Выйти"
        confirmColor="error"
        onConfirm={() => {
          logout();
          navigate("/login", { replace: true });
        }}
        onClose={() => setConfirmLogout(false)}
      />
    </Stack>
  );
}
