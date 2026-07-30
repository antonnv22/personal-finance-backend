import { Avatar, Box, Paper, Stack, Typography, useTheme } from "@mui/material";
import type { ReactNode } from "react";
import WalletRoundedIcon from "@mui/icons-material/WalletRounded";
import TrendingUpRoundedIcon from "@mui/icons-material/TrendingUpRounded";
import PieChartRoundedIcon from "@mui/icons-material/PieChartRounded";
import ShieldRoundedIcon from "@mui/icons-material/ShieldRounded";

interface AuthLayoutProps {
  title: string;
  subtitle: string;
  children: ReactNode;
}

const HIGHLIGHTS = [
  { icon: TrendingUpRoundedIcon, text: "Наглядная динамика доходов и расходов" },
  { icon: PieChartRoundedIcon, text: "Разбивка трат по категориям" },
  { icon: ShieldRoundedIcon, text: "Данные защищены и доступны только вам" },
];

export function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  const theme = useTheme();

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        bgcolor: "background.default",
      }}
    >
      <Box
        sx={{
          display: { xs: "none", md: "flex" },
          flexDirection: "column",
          justifyContent: "space-between",
          width: "42%",
          p: 6,
          color: "#fff",
          background: `linear-gradient(155deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main})`,
        }}
      >
        <Stack direction="row" spacing={1.5} alignItems="center">
          <Avatar sx={{ bgcolor: "rgba(255,255,255,0.16)" }}>
            <WalletRoundedIcon />
          </Avatar>
          <Typography variant="h6">Personal Finance</Typography>
        </Stack>

        <Box>
          <Typography variant="h4" sx={{ mb: 2, fontWeight: 700, maxWidth: 420 }}>
            Управляйте личными финансами осознанно
          </Typography>
          <Stack spacing={2} sx={{ mt: 4 }}>
            {HIGHLIGHTS.map(({ icon: Icon, text }) => (
              <Stack key={text} direction="row" spacing={1.5} alignItems="center">
                <Avatar sx={{ bgcolor: "rgba(255,255,255,0.16)", width: 34, height: 34 }}>
                  <Icon fontSize="small" />
                </Avatar>
                <Typography variant="body2">{text}</Typography>
              </Stack>
            ))}
          </Stack>
        </Box>

        <Typography variant="caption" sx={{ opacity: 0.7 }}>
          © {new Date().getFullYear()} Personal Finance
        </Typography>
      </Box>

      <Box
        sx={{
          flex: 1,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          p: { xs: 2, sm: 4 },
        }}
      >
        <Paper
          elevation={0}
          sx={{
            width: "100%",
            maxWidth: 420,
            p: { xs: 3, sm: 4.5 },
            border: "1px solid",
            borderColor: "divider",
          }}
        >
          <Typography variant="h5" gutterBottom>
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {subtitle}
          </Typography>
          {children}
        </Paper>
      </Box>
    </Box>
  );
}
