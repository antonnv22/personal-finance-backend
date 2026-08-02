import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Stack,
  Typography,
} from "@mui/material";
import type { CalendarItemResponse } from "@/api/types";
import {
  DISPLAY_STATUS_LABELS,
  displayStatusColor,
  formatDate,
  formatDeviation,
  formatMoney,
  toDisplayStatus,
} from "@/utils/format";

interface OccurrenceDetailsDialogProps {
  open: boolean;
  occurrence: CalendarItemResponse | null;
  onClose: () => void;
}

function Row({ label, value, emphasis }: { label: string; value: string; emphasis?: string }) {
  return (
    <Stack direction="row" justifyContent="space-between" alignItems="baseline">
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body1" fontWeight={600} color={emphasis}>
        {value}
      </Typography>
    </Stack>
  );
}

export function OccurrenceDetailsDialog({ open, occurrence, onClose }: OccurrenceDetailsDialogProps) {
  if (!occurrence) {
    return null;
  }

  const status = toDisplayStatus(occurrence);
  const color = displayStatusColor(status);
  const deviation = occurrence.deviation;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>
        <Stack direction="row" spacing={1.5} alignItems="center">
          <span>{occurrence.name}</span>
          <Chip
            label={DISPLAY_STATUS_LABELS[status]}
            size="small"
            sx={{ bgcolor: color.main, color: color.contrast }}
          />
        </Stack>
      </DialogTitle>
      <DialogContent>
        <Stack spacing={1.5} sx={{ mt: 0.5 }}>
          <Row label="План" value={formatMoney(occurrence.plannedAmount, occurrence.currency)} />
          <Row label="Плановая дата" value={formatDate(occurrence.date)} />

          {occurrence.status === "COMPLETED" && (
            <>
              <Divider sx={{ my: 1 }} />
              <Row
                label="Факт"
                value={formatMoney(occurrence.actualAmount ?? 0, occurrence.currency)}
              />
              {occurrence.actualDate && (
                <Row label="Фактическая дата" value={formatDate(occurrence.actualDate)} />
              )}
              {deviation !== null && (
                <Row
                  label="Отклонение"
                  value={formatDeviation(deviation, occurrence.currency)}
                  emphasis={
                    deviation > 0 ? "error.main" : deviation < 0 ? "success.main" : "text.primary"
                  }
                />
              )}
            </>
          )}

          <Divider sx={{ my: 1 }} />
          <Row label="Счёт" value={occurrence.accountName} />
          <Row label="Категория" value={occurrence.categoryName} />

          {occurrence.status === "SKIPPED" && (
            <Box sx={{ mt: 1 }}>
              <Typography variant="body2" color="text.secondary">
                Платёж пропущен — расход не создавался.
              </Typography>
            </Box>
          )}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5 }}>
        <Button onClick={onClose} variant="contained">
          Закрыть
        </Button>
      </DialogActions>
    </Dialog>
  );
}
