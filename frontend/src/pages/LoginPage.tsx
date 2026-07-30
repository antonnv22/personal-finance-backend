import { useState, type FormEvent } from "react";
import { Alert, Box, Button, Link as MuiLink, Stack, TextField } from "@mui/material";
import { Link as RouterLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/useAuth";
import { extractErrorMessage } from "@/api/client";
import { AuthLayout } from "@/components/layout/AuthLayout";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const from = (location.state as { from?: { pathname: string } } | null)?.from?.pathname ?? "/";

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Неверный email или пароль"));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout title="Вход в аккаунт" subtitle="Введите данные, чтобы продолжить работу с бюджетом">
      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Stack spacing={2.5}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoFocus
            fullWidth
          />
          <TextField
            label="Пароль"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={submitting} fullWidth>
            {submitting ? "Входим..." : "Войти"}
          </Button>
          <MuiLink component={RouterLink} to="/register" variant="body2" textAlign="center">
            Нет аккаунта? Зарегистрироваться
          </MuiLink>
        </Stack>
      </Box>
    </AuthLayout>
  );
}
