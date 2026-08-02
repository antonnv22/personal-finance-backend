import { useEffect, useMemo, useState, type FormEvent } from "react";
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
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material";
import type { TransactionType } from "@/api/types";
import { useAccounts } from "@/hooks/useAccounts";
import { useCategories } from "@/hooks/useCategories";
import { useCreateTransaction } from "@/hooks/useTransactions";
import { extractErrorMessage } from "@/api/client";
import { todayIso } from "@/utils/format";

interface TransactionFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSaved: (message: string) => void;
}

export function TransactionFormDialog({ open, onClose, onSaved }: TransactionFormDialogProps) {
  const accountsQuery = useAccounts();
  const [type, setType] = useState<TransactionType>("EXPENSE");
  const categoriesQuery = useCategories(type);
  const createTransaction = useCreateTransaction();

  const [accountId, setAccountId] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [transactionDate, setTransactionDate] = useState(todayIso());
  const [error, setError] = useState<string | null>(null);

  const activeAccounts = useMemo(
    () => (accountsQuery.data ?? []).filter((a) => !a.archived),
    [accountsQuery.data]
  );

  useEffect(() => {
    if (open) {
      setType("EXPENSE");
      setAccountId("");
      setCategoryId("");
      setAmount("");
      setDescription("");
      setTransactionDate(todayIso());
      setError(null);
    }
  }, [open]);

  useEffect(() => {
    setCategoryId("");
  }, [type]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const numericAmount = Number(amount.replace(",", "."));
    if (!accountId || !categoryId || !numericAmount || numericAmount <= 0) {
      setError("Заполните сумму, счёт и категорию");
      return;
    }

    try {
      await createTransaction.mutateAsync({
        amount: numericAmount.toFixed(2),
        type,
        accountId,
        categoryId,
        description: description.trim() || null,
        transactionDate,
      });
      onSaved("Транзакция добавлена");
      onClose();
    } catch (err) {
      setError(extractErrorMessage(err, "Не удалось создать транзакцию"));
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Новая транзакция</DialogTitle>
      <Stack component="form" onSubmit={handleSubmit}>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 0.5 }}>
            {error && <Alert severity="error">{error}</Alert>}

            <ToggleButtonGroup
              value={type}
              exclusive
              fullWidth
              onChange={(_, value: TransactionType | null) => value && setType(value)}
            >
              <ToggleButton value="EXPENSE" color="error">
                Расход
              </ToggleButton>
              <ToggleButton value="INCOME" color="success">
                Доход
              </ToggleButton>
            </ToggleButtonGroup>

            <TextField
              label="Сумма"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
              autoFocus
              fullWidth
              inputMode="decimal"
              placeholder="0.00"
            />

            <TextField
              select
              label="Счёт"
              value={accountId}
              onChange={(e) => setAccountId(e.target.value)}
              required
              fullWidth
              disabled={activeAccounts.length === 0}
              helperText={activeAccounts.length === 0 ? "Сначала создайте счёт" : undefined}
            >
              {activeAccounts.map((account) => (
                <MenuItem key={account.id} value={account.id}>
                  {account.name}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              select
              label="Категория"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              required
              fullWidth
              disabled={!categoriesQuery.data || categoriesQuery.data.length === 0}
              helperText={
                categoriesQuery.data && categoriesQuery.data.length === 0
                  ? "Сначала создайте категорию этого типа"
                  : undefined
              }
            >
              {(categoriesQuery.data ?? []).map((category) => (
                <MenuItem key={category.id} value={category.id}>
                  {category.name}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              label="Дата"
              type="date"
              value={transactionDate}
              onChange={(e) => setTransactionDate(e.target.value)}
              required
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
            />

            <TextField
              label="Описание"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              fullWidth
              multiline
              minRows={2}
              slotProps={{ htmlInput: { maxLength: 500 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2.5 }}>
          <Button onClick={onClose} color="inherit" disabled={createTransaction.isPending}>
            Отмена
          </Button>
          <Button type="submit" variant="contained" disabled={createTransaction.isPending}>
            Создать
          </Button>
        </DialogActions>
      </Stack>
    </Dialog>
  );
}
