import { useEffect, useState, type FormEvent } from "react";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import type { AccountResponse, AccountType, Currency } from "@/api/types";
import { ACCOUNT_TYPES, CURRENCIES } from "@/api/types";
import { ACCOUNT_TYPE_LABELS, CURRENCY_LABELS } from "@/utils/format";
import { useCreateAccount, useUpdateAccount } from "@/hooks/useAccounts";
import { extractErrorMessage } from "@/api/client";

interface AccountFormDialogProps {
  open: boolean;
  onClose: () => void;
  account: AccountResponse | null;
  onSaved: (message: string) => void;
}

export function AccountFormDialog({ open, onClose, account, onSaved }: AccountFormDialogProps) {
  const isEdit = Boolean(account);
  const createAccount = useCreateAccount();
  const updateAccount = useUpdateAccount();

  const [name, setName] = useState("");
  const [type, setType] = useState<AccountType>("CASH");
  const [currency, setCurrency] = useState<Currency>("RUB");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setName(account?.name ?? "");
      setType(account?.type ?? "CASH");
      setCurrency(account?.currency ?? "RUB");
      setError(null);
    }
  }, [open, account]);

  const submitting = createAccount.isPending || updateAccount.isPending;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      if (isEdit && account) {
        await updateAccount.mutateAsync({ id: account.id, payload: { name, type, currency } });
        onSaved("Счёт обновлён");
      } else {
        await createAccount.mutateAsync({ name, type, currency });
        onSaved("Счёт создан");
      }
      onClose();
    } catch (err) {
      setError(extractErrorMessage(err, "Не удалось сохранить счёт"));
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{isEdit ? "Изменить счёт" : "Новый счёт"}</DialogTitle>
      <Stack component="form" onSubmit={handleSubmit}>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 0.5 }}>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField
              label="Название"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              autoFocus
              fullWidth
            />
            <TextField
              select
              label="Тип счёта"
              value={type}
              onChange={(e) => setType(e.target.value as AccountType)}
              fullWidth
            >
              {ACCOUNT_TYPES.map((t) => (
                <MenuItem key={t} value={t}>
                  {ACCOUNT_TYPE_LABELS[t]}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Валюта"
              value={currency}
              onChange={(e) => setCurrency(e.target.value as Currency)}
              fullWidth
            >
              {CURRENCIES.map((c) => (
                <MenuItem key={c} value={c}>
                  {CURRENCY_LABELS[c]}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2.5 }}>
          <Button onClick={onClose} color="inherit" disabled={submitting}>
            Отмена
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {isEdit ? "Сохранить" : "Создать"}
          </Button>
        </DialogActions>
      </Stack>
    </Dialog>
  );
}
