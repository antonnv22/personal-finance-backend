import { useMemo, useState } from "react";
import {
  Box,
  Card,
  CardContent,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Stack,
  Tooltip,
  Typography,
  useMediaQuery,
} from "@mui/material";
import { useTheme } from "@mui/material/styles";
import ChevronLeftRoundedIcon from "@mui/icons-material/ChevronLeftRounded";
import ChevronRightRoundedIcon from "@mui/icons-material/ChevronRightRounded";
import TodayRoundedIcon from "@mui/icons-material/TodayRounded";
import MoreVertRoundedIcon from "@mui/icons-material/MoreVertRounded";
import EventRepeatRoundedIcon from "@mui/icons-material/EventRepeatRounded";
import { useSnackbar } from "notistack";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { useCalendarMonth } from "@/hooks/useCalendar";
import {
  useDeleteRecurringExpense,
  useRecurringExpenses,
  useSkipOccurrence,
} from "@/hooks/useRecurringExpenses";
import type { CalendarItemResponse, RecurringExpenseResponse } from "@/api/types";
import {
  DISPLAY_STATUS_LABELS,
  displayStatusColor,
  formatMoney,
  formatTotal,
  formatTotalDeviation,
  MONTH_LABELS,
  toDisplayStatus,
  WEEKDAY_LABELS,
} from "@/utils/format";
import { extractErrorMessage } from "@/api/client";
import { CompleteOccurrenceDialog } from "./CompleteOccurrenceDialog";
import { OccurrenceDetailsDialog } from "./OccurrenceDetailsDialog";
import { RecurringExpenseFormDialog } from "./RecurringExpenseFormDialog";

interface DayCell {
  date: Date | null;
  iso: string;
  items: CalendarItemResponse[];
}

/** Сетка месяца, выровненная по понедельникам, с пустыми ячейками по краям. */
function buildGrid(year: number, month: number, items: CalendarItemResponse[]): DayCell[] {
  const byDate = new Map<string, CalendarItemResponse[]>();
  for (const item of items) {
    const list = byDate.get(item.date) ?? [];
    list.push(item);
    byDate.set(item.date, list);
  }

  const firstDay = new Date(year, month - 1, 1);
  const daysInMonth = new Date(year, month, 0).getDate();
  // getDay(): 0 — воскресенье; приводим к понедельнику как первому дню недели.
  const leadingBlanks = (firstDay.getDay() + 6) % 7;

  const cells: DayCell[] = [];
  for (let i = 0; i < leadingBlanks; i++) {
    cells.push({ date: null, iso: `blank-start-${i}`, items: [] });
  }
  for (let day = 1; day <= daysInMonth; day++) {
    const iso = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    cells.push({ date: new Date(year, month - 1, day), iso, items: byDate.get(iso) ?? [] });
  }
  while (cells.length % 7 !== 0) {
    cells.push({ date: null, iso: `blank-end-${cells.length}`, items: [] });
  }
  return cells;
}

export function CalendarPage() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const { enqueueSnackbar } = useSnackbar();

  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);

  const calendarQuery = useCalendarMonth(year, month);
  const rulesQuery = useRecurringExpenses();
  const skipOccurrence = useSkipOccurrence();
  const deleteRule = useDeleteRecurringExpense();

  const [completeTarget, setCompleteTarget] = useState<CalendarItemResponse | null>(null);
  const [detailsTarget, setDetailsTarget] = useState<CalendarItemResponse | null>(null);
  const [skipTarget, setSkipTarget] = useState<CalendarItemResponse | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<RecurringExpenseResponse | null>(null);
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const [menuRule, setMenuRule] = useState<RecurringExpenseResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<RecurringExpenseResponse | null>(null);

  const items = calendarQuery.data?.items;
  const grid = useMemo(() => buildGrid(year, month, items ?? []), [year, month, items]);
  const summary = calendarQuery.data?.summary;
  const todayIso = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(
    today.getDate()
  ).padStart(2, "0")}`;

  function shiftMonth(delta: number) {
    const next = new Date(year, month - 1 + delta, 1);
    setYear(next.getFullYear());
    setMonth(next.getMonth() + 1);
  }

  function goToToday() {
    setYear(today.getFullYear());
    setMonth(today.getMonth() + 1);
  }

  function openItem(item: CalendarItemResponse) {
    if (item.status === "PLANNED") {
      setCompleteTarget(item);
    } else {
      setDetailsTarget(item);
    }
  }

  async function handleSkip() {
    if (!skipTarget) return;
    try {
      await skipOccurrence.mutateAsync(skipTarget.occurrenceId);
      enqueueSnackbar("Платёж пропущен", { variant: "success" });
      setSkipTarget(null);
    } catch (err) {
      enqueueSnackbar(extractErrorMessage(err, "Не удалось пропустить платёж"), { variant: "error" });
    }
  }

  async function handleDeleteRule() {
    if (!deleteTarget) return;
    try {
      await deleteRule.mutateAsync(deleteTarget.id);
      enqueueSnackbar("Правило удалено (или архивировано, если по нему есть оплаты)", {
        variant: "success",
      });
      setDeleteTarget(null);
    } catch (err) {
      enqueueSnackbar(extractErrorMessage(err, "Не удалось удалить правило"), { variant: "error" });
    }
  }

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Календарь"
        subtitle="Регулярные расходы: план и факт"
        actionLabel="Добавить правило"
        onAction={() => {
          setEditingRule(null);
          setFormOpen(true);
        }}
      />

      {summary && (
        <Box
          sx={{
            display: "grid",
            gap: 2.5,
            gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
          }}
        >
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Запланировано
              </Typography>
              <Typography variant="h5">
                {formatTotal(summary.plannedTotal, summary.currency)}
              </Typography>
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Фактически
              </Typography>
              <Typography variant="h5">
                {formatTotal(summary.actualTotal, summary.currency)}
              </Typography>
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Отклонение
              </Typography>
              <Typography
                variant="h5"
                color={
                  summary.deviation > 0
                    ? "error.main"
                    : summary.deviation < 0
                      ? "success.main"
                      : "text.primary"
                }
              >
                {formatTotalDeviation(summary.deviation, summary.currency)}
              </Typography>
              {!summary.currency && summary.plannedTotal !== 0 && (
                <Typography variant="caption" color="text.secondary">
                  Несколько валют — итог без конвертации
                </Typography>
              )}
            </CardContent>
          </Card>
        </Box>
      )}

      <Card>
        <CardContent>
          <Stack
            direction="row"
            alignItems="center"
            justifyContent="space-between"
            sx={{ mb: 2 }}
          >
            <Typography variant="subtitle1">
              {MONTH_LABELS[month - 1]} {year}
            </Typography>
            <Stack direction="row" spacing={0.5}>
              <Tooltip title="Предыдущий месяц">
                <IconButton onClick={() => shiftMonth(-1)} size="small">
                  <ChevronLeftRoundedIcon />
                </IconButton>
              </Tooltip>
              <Tooltip title="Текущий месяц">
                <IconButton onClick={goToToday} size="small">
                  <TodayRoundedIcon />
                </IconButton>
              </Tooltip>
              <Tooltip title="Следующий месяц">
                <IconButton onClick={() => shiftMonth(1)} size="small">
                  <ChevronRightRoundedIcon />
                </IconButton>
              </Tooltip>
            </Stack>
          </Stack>

          {calendarQuery.isLoading ? (
            <LoadingState minHeight={320} />
          ) : calendarQuery.isError ? (
            <ErrorState error={calendarQuery.error} onRetry={() => calendarQuery.refetch()} />
          ) : (
            <Box sx={{ overflowX: "auto" }}>
              <Box sx={{ minWidth: isMobile ? 560 : "auto" }}>
                <Box
                  sx={{
                    display: "grid",
                    gridTemplateColumns: "repeat(7, 1fr)",
                    gap: 1,
                    mb: 1,
                  }}
                >
                  {WEEKDAY_LABELS.map((label) => (
                    <Typography
                      key={label}
                      variant="caption"
                      color="text.secondary"
                      textAlign="center"
                      fontWeight={600}
                    >
                      {label}
                    </Typography>
                  ))}
                </Box>

                <Box sx={{ display: "grid", gridTemplateColumns: "repeat(7, 1fr)", gap: 1 }}>
                  {grid.map((cell) => {
                    if (!cell.date) {
                      return <Box key={cell.iso} sx={{ minHeight: 96 }} />;
                    }
                    const isToday = cell.iso === todayIso;
                    return (
                      <Box
                        key={cell.iso}
                        sx={{
                          minHeight: 96,
                          borderRadius: 2,
                          border: "1px solid",
                          borderColor: isToday ? "primary.main" : "divider",
                          bgcolor: isToday ? "action.hover" : "transparent",
                          p: 0.75,
                          display: "flex",
                          flexDirection: "column",
                          gap: 0.5,
                        }}
                      >
                        <Typography
                          variant="caption"
                          fontWeight={isToday ? 700 : 500}
                          color={isToday ? "primary.main" : "text.secondary"}
                        >
                          {cell.date.getDate()}
                        </Typography>

                        {cell.items.map((item) => {
                          const status = toDisplayStatus(item);
                          const color = displayStatusColor(status);
                          return (
                            <Tooltip
                              key={item.occurrenceId}
                              title={`${item.name} · ${DISPLAY_STATUS_LABELS[status]}`}
                            >
                              <Box
                                onClick={() => openItem(item)}
                                sx={{
                                  cursor: "pointer",
                                  bgcolor: color.main,
                                  color: color.contrast,
                                  borderRadius: 1,
                                  px: 0.75,
                                  py: 0.4,
                                  transition: "opacity 120ms",
                                  "&:hover": { opacity: 0.85 },
                                }}
                              >
                                <Typography variant="caption" display="block" noWrap fontWeight={600}>
                                  {item.name}
                                </Typography>
                                <Typography variant="caption" display="block" noWrap>
                                  {formatMoney(
                                    item.actualAmount ?? item.plannedAmount,
                                    item.currency
                                  )}
                                </Typography>
                              </Box>
                            </Tooltip>
                          );
                        })}
                      </Box>
                    );
                  })}
                </Box>
              </Box>
            </Box>
          )}

          <Stack direction="row" spacing={2} flexWrap="wrap" sx={{ mt: 2 }}>
            {(["PLANNED", "COMPLETED", "SKIPPED", "OVERDUE"] as const).map((status) => (
              <Stack key={status} direction="row" spacing={0.75} alignItems="center">
                <Box
                  sx={{
                    width: 12,
                    height: 12,
                    borderRadius: 0.5,
                    bgcolor: displayStatusColor(status).main,
                  }}
                />
                <Typography variant="caption" color="text.secondary">
                  {DISPLAY_STATUS_LABELS[status]}
                </Typography>
              </Stack>
            ))}
          </Stack>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Правила
          </Typography>
          {rulesQuery.isLoading ? (
            <LoadingState minHeight={140} />
          ) : rulesQuery.isError ? (
            <ErrorState error={rulesQuery.error} onRetry={() => rulesQuery.refetch()} />
          ) : rulesQuery.data && rulesQuery.data.length > 0 ? (
            <Stack spacing={1}>
              {rulesQuery.data.map((rule) => (
                <Stack
                  key={rule.id}
                  direction="row"
                  alignItems="center"
                  justifyContent="space-between"
                  sx={{ py: 1, borderBottom: "1px solid", borderColor: "divider" }}
                >
                  <Box sx={{ minWidth: 0 }}>
                    <Stack direction="row" spacing={1} alignItems="center">
                      <Typography variant="body2" fontWeight={600} noWrap>
                        {rule.name}
                      </Typography>
                      {!rule.active && <Chip label="Архив" size="small" />}
                    </Stack>
                    <Typography variant="caption" color="text.secondary">
                      каждое {rule.dayOfMonth}-е · {rule.categoryName} · {rule.accountName}
                    </Typography>
                  </Box>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Typography variant="body2" fontWeight={600}>
                      {formatMoney(rule.plannedAmount, rule.currency)}
                    </Typography>
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        setMenuAnchor(e.currentTarget);
                        setMenuRule(rule);
                      }}
                    >
                      <MoreVertRoundedIcon fontSize="small" />
                    </IconButton>
                  </Stack>
                </Stack>
              ))}
            </Stack>
          ) : (
            <EmptyState
              icon={<EventRepeatRoundedIcon fontSize="large" color="disabled" />}
              title="Пока нет регулярных расходов"
              description="Добавьте правило — например, подписку, аренду или кредит"
              actionLabel="Добавить правило"
              onAction={() => {
                setEditingRule(null);
                setFormOpen(true);
              }}
            />
          )}
        </CardContent>
      </Card>

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
        <MenuItem
          onClick={() => {
            setEditingRule(menuRule);
            setFormOpen(true);
            setMenuAnchor(null);
          }}
        >
          Редактировать
        </MenuItem>
        <MenuItem
          onClick={() => {
            setDeleteTarget(menuRule);
            setMenuAnchor(null);
          }}
          sx={{ color: "error.main" }}
        >
          Удалить
        </MenuItem>
      </Menu>

      <CompleteOccurrenceDialog
        open={Boolean(completeTarget)}
        occurrence={completeTarget}
        onClose={() => setCompleteTarget(null)}
        onCompleted={(message) => enqueueSnackbar(message, { variant: "success" })}
        onSkip={(occurrence) => {
          setCompleteTarget(null);
          setSkipTarget(occurrence);
        }}
      />

      <OccurrenceDetailsDialog
        open={Boolean(detailsTarget)}
        occurrence={detailsTarget}
        onClose={() => setDetailsTarget(null)}
      />

      <RecurringExpenseFormDialog
        open={formOpen}
        rule={editingRule}
        onClose={() => setFormOpen(false)}
        onSaved={(message) => enqueueSnackbar(message, { variant: "success" })}
      />

      <ConfirmDialog
        open={Boolean(skipTarget)}
        title="Пропустить платёж?"
        description="Расход создан не будет, платёж останется в истории как пропущенный."
        confirmLabel="Пропустить"
        loading={skipOccurrence.isPending}
        onConfirm={handleSkip}
        onClose={() => setSkipTarget(null)}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Удалить правило?"
        description={`Правило «${deleteTarget?.name}» будет удалено. Если по нему уже есть оплаченные платежи, оно будет заархивировано, а история сохранится.`}
        confirmLabel="Удалить"
        confirmColor="error"
        loading={deleteRule.isPending}
        onConfirm={handleDeleteRule}
        onClose={() => setDeleteTarget(null)}
      />
    </Stack>
  );
}
