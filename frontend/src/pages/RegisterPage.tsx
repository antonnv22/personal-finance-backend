import { useState, type FormEvent } from "react";
import { Alert, Box, Button, Link as MuiLink, Stack, TextField } from "@mui/material";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/useAuth";
import { extractErrorMessage } from "@/api/client";
import { AuthLayout } from "@/components/layout/AuthLayout";

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    if (password.length < 6) {
      setError("Пароль должен содержать не менее 6 символов");
      return;
    }
    if (password !== confirmPassword) {
      setError("Пароли не совпадают");
      return;
    }

    setSubmitting(true);
    try {
      await register(email, password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Не удалось зарегистрироваться"));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout title="Создать аккаунт" subtitle="Заполните форму, чтобы начать вести учёт финансов">
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
            helperText="Минимум 6 символов"
          />
          <TextField
            label="Подтверждение пароля"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={submitting} fullWidth>
            {submitting ? "Создаём аккаунт..." : "Зарегистрироваться"}
          </Button>
          <MuiLink component={RouterLink} to="/login" variant="body2" textAlign="center">
            Уже есть аккаунт? Войти
          </MuiLink>
        </Stack>
      </Box>
    </AuthLayout>
  );
}
