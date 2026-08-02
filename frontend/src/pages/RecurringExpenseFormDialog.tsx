import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Stack,
  Switch,
  TextField,
} from "@mui/material";
import type { Currency, RecurringExpenseResponse } from "@/api/types";
import { useAccounts } from "@/hooks/useAccounts";
import { useCategories } from "@/hooks/useCategories";
import { useCreateRecurringExpense, useUpdateRecurringExpense } from "@/hooks/useRecurringExpenses";
import { extractErrorMessage } from "@/api/client";
import { todayIso } from "@/utils/format";

interface RecurringExpenseFormDialogProps {
  open: boolean;
  rule: RecurringExpenseResponse | null;
  onClose: () => void;
  onSaved: (message: string) => void;
}

const DAYS = Array.from({ length: 31 }, (_, i) => i + 1);

export function RecurringExpenseFormDialog({
  open,
  rule,
  onClose,
  onSaved,
}: RecurringExpenseFormDialogProps) {
  const isEdit = Boolean(rule);
  const accountsQuery = useAccounts();
  const categoriesQuery = useCategories("EXPENSE");
  const createRule = useCreateRecurringExpense();
  const updateRule = useUpdateRecurringExpense();

  const [name, setName] = useState("");
  const [plannedAmount, setPlannedAmount] = useState("");
  const [accountId, setAccountId] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [dayOfMonth, setDayOfMonth] = useState(1);
  const [startDate, setStartDate] = useState(todayIso());
  const [endDate, setEndDate] = useState("");
  const [description, setDescription] = useState("");
  const [active, setActive] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const activeAccounts = useMemo(
    () => (accountsQuery.data ?? []).filter((a) => !a.archived),
    [accountsQuery.data]
  );

  // Валюта правила всегда равна валюте выбранного счёта — так сравнение
  // плана и факта остаётся осмысленным.
  const selectedAccount = activeAccounts.find((a) => a.id === accountId);
  const currency: Currency = selectedAccount?.currency ?? "RUB";

  useEffect(() => {
    if (!open) return;
    setName(rule?.name ?? "");
    setPlannedAmount(rule ? String(rule.plannedAmount) : "");
    setAccountId(rule?.accountId ?? "");
    setCategoryId(rule?.categoryId ?? "");
    setDayOfMonth(rule?.dayOfMonth ?? 1);
    setStartDate(rule?.startDate ?? todayIso());
    setEndDate(rule?.endDate ?? "");
    setDescription(rule?.description ?? "");
    setActive(rule?.active ?? true);
    setError(null);
  }, [open, rule]);

  const submitting = createRule.isPending || updateRule.isPending;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const parsedAmount = Number(plannedAmount.replace(",", "."));
    if (!parsedAmount || parsedAmount <= 0) {
      setError("Укажите сумму больше нуля");
      return;
    }
    if (!accountId || !categoryId) {
      setError("Выберите счёт и категорию");
      return;
    }

    const payload = {
      name,
      description: description.trim() || null,
      plannedAmount: parsedAmount.toFixed(2),
      currency,
      accountId,
      categoryId,
      recurrenceType: "MONTHLY" as const,
      dayOfMonth,
      startDate,
      endDate: endDate || null,
    };

    try {
      if (isEdit && rule) {
        await updateRule.mutateAsync({ id: rule.id, payload: { ...payload, active } });
        onSaved("Правило обновлено");
      } else {
        await createRule.mutateAsync(payload);
        onSaved("Правило создано");
      }
      onClose();
    } catch (err) {
      setError(extractErrorMessage(err, "Не удалось сохранить правило"));
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{isEdit ? "Изменить правило" : "Новый регулярный расход"}</DialogTitle>
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
              placeholder="Netflix"
            />

            <TextField
              select
              label="Счёт"
              value={accountId}
              onChange={(e) => setAccountId(e.target.value)}
              required
              fullWidth
              disabled={activeAccounts.length === 0}
              helperText={
                activeAccounts.length === 0
                  ? "Сначала создайте счёт"
                  : `Валюта правила: ${currency}`
              }
            >
              {activeAccounts.map((account) => (
                <MenuItem key={account.id} value={account.id}>
                  {account.name} ({account.currency})
                </MenuItem>
              ))}
            </TextField>

            <TextField
              label="Сумма"
              value={plannedAmount}
              onChange={(e) => setPlannedAmount(e.target.value)}
              required
              fullWidth
              inputMode="decimal"
              placeholder="15.00"
            />

            <TextField
              select
              label="Категория"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              required
              fullWidth
              disabled={!categoriesQuery.data?.length}
              helperText={
                categoriesQuery.data && categoriesQuery.data.length === 0
                  ? "Сначала создайте категорию расхода"
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
              select
              label="День месяца"
              value={dayOfMonth}
              onChange={(e) => setDayOfMonth(Number(e.target.value))}
              fullWidth
              helperText="В коротких месяцах платёж сдвигается на последний день"
            >
              {DAYS.map((day) => (
                <MenuItem key={day} value={day}>
                  {day}
                </MenuItem>
              ))}
            </TextField>

            <Stack direction="row" spacing={2}>
              <TextField
                label="Начало"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                fullWidth
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                label="Окончание"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                fullWidth
                slotProps={{ inputLabel: { shrink: true } }}
                helperText="Не обязательно"
              />
            </Stack>

            <TextField
              label="Описание"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              fullWidth
              multiline
              minRows={2}
              slotProps={{ htmlInput: { maxLength: 500 } }}
            />

            {isEdit && (
              <FormControlLabel
                control={<Switch checked={active} onChange={(e) => setActive(e.target.checked)} />}
                label="Активно"
              />
            )}
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
