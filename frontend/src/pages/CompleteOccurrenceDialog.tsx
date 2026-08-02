import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import type { CalendarItemResponse } from "@/api/types";
import { useAccounts } from "@/hooks/useAccounts";
import { useCompleteOccurrence } from "@/hooks/useRecurringExpenses";
import { extractErrorMessage } from "@/api/client";
import { formatDate, formatMoney } from "@/utils/format";

interface CompleteOccurrenceDialogProps {
  open: boolean;
  occurrence: CalendarItemResponse | null;
  onClose: () => void;
  onCompleted: (message: string) => void;
  onSkip: (occurrence: CalendarItemResponse) => void;
}

export function CompleteOccurrenceDialog({
  open,
  occurrence,
  onClose,
  onCompleted,
  onSkip,
}: CompleteOccurrenceDialogProps) {
  const accountsQuery = useAccounts();
  const completeOccurrence = useCompleteOccurrence();

  const [actualDate, setActualDate] = useState("");
  const [actualAmount, setActualAmount] = useState("");
  const [accountId, setAccountId] = useState("");
  const [comment, setComment] = useState("");
  const [error, setError] = useState<string | null>(null);

  // Транзакция не хранит валюту — сумма всегда в валюте счёта, поэтому
  // платить можно только со счёта той же валюты, что и правило.
  const eligibleAccounts = useMemo(
    () =>
      (accountsQuery.data ?? []).filter(
        (account) => !account.archived && account.currency === occurrence?.currency
      ),
    [accountsQuery.data, occurrence?.currency]
  );

  useEffect(() => {
    if (open && occurrence) {
      setActualDate(occurrence.date);
      setActualAmount(String(occurrence.plannedAmount));
      setAccountId("");
      setComment(occurrence.name);
      setError(null);
    }
  }, [open, occurrence]);

  if (!occurrence) {
    return null;
  }

  const deviationPreview = (() => {
    const parsed = Number(actualAmount.replace(",", "."));
    if (!parsed || Number.isNaN(parsed)) return null;
    return parsed - occurrence.plannedAmount;
  })();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const parsedAmount = Number(actualAmount.replace(",", "."));
    if (!parsedAmount || parsedAmount <= 0) {
      setError("Укажите сумму больше нуля");
      return;
    }
    if (!accountId) {
      setError("Выберите счёт");
      return;
    }

    try {
      await completeOccurrence.mutateAsync({
        id: occurrence!.occurrenceId,
        payload: {
          actualDate,
          actualAmount: parsedAmount.toFixed(2),
          accountId,
          comment: comment.trim() || null,
        },
      });
      onCompleted("Расход создан");
      onClose();
    } catch (err) {
      setError(extractErrorMessage(err, "Не удалось создать расход"));
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{occurrence.name}</DialogTitle>
      <Stack component="form" onSubmit={handleSubmit}>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 0.5 }}>
            {error && <Alert severity="error">{error}</Alert>}

            <Box
              sx={{
                bgcolor: "action.hover",
                borderRadius: 2,
                px: 2,
                py: 1.5,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                Запланировано
              </Typography>
              <Typography variant="body1" fontWeight={600}>
                {formatDate(occurrence.date)} · {formatMoney(occurrence.plannedAmount, occurrence.currency)}
              </Typography>
            </Box>

            <Typography variant="subtitle2">Фактический расход</Typography>

            <TextField
              label="Дата"
              type="date"
              value={actualDate}
              onChange={(e) => setActualDate(e.target.value)}
              required
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
            />

            <TextField
              label="Сумма"
              value={actualAmount}
              onChange={(e) => setActualAmount(e.target.value)}
              required
              fullWidth
              inputMode="decimal"
              helperText={
                deviationPreview !== null && deviationPreview !== 0
                  ? `Отклонение: ${deviationPreview > 0 ? "+" : "−"}${formatMoney(
                      Math.abs(deviationPreview),
                      occurrence.currency
                    )}`
                  : undefined
              }
            />

            <TextField
              select
              label="Счёт"
              value={accountId}
              onChange={(e) => setAccountId(e.target.value)}
              required
              fullWidth
              disabled={eligibleAccounts.length === 0}
              helperText={
                eligibleAccounts.length === 0
                  ? `Нет счетов в валюте ${occurrence.currency}`
                  : undefined
              }
            >
              {eligibleAccounts.map((account) => (
                <MenuItem key={account.id} value={account.id}>
                  {account.name}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              label="Комментарий"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              fullWidth
              multiline
              minRows={2}
              slotProps={{ htmlInput: { maxLength: 500 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2.5, justifyContent: "space-between" }}>
          <Button
            onClick={() => onSkip(occurrence)}
            color="warning"
            disabled={completeOccurrence.isPending}
          >
            Пропустить
          </Button>
          <Stack direction="row" spacing={1}>
            <Button onClick={onClose} color="inherit" disabled={completeOccurrence.isPending}>
              Отмена
            </Button>
            <Button type="submit" variant="contained" disabled={completeOccurrence.isPending}>
              Создать расход
            </Button>
          </Stack>
        </DialogActions>
      </Stack>
    </Dialog>
  );
}
