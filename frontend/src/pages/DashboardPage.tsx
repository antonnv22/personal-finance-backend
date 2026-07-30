import { useMemo } from "react";
import {
  Box,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
  useTheme,
} from "@mui/material";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import TrendingUpRoundedIcon from "@mui/icons-material/TrendingUpRounded";
import TrendingDownRoundedIcon from "@mui/icons-material/TrendingDownRounded";
import AccountBalanceWalletRoundedIcon from "@mui/icons-material/AccountBalanceWalletRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import EventRepeatRoundedIcon from "@mui/icons-material/EventRepeatRounded";
import { Link as RouterLink } from "react-router-dom";
import { StatCard } from "@/components/common/StatCard";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { useAccounts } from "@/hooks/useAccounts";
import { useExpensesByCategory, useMonthlyReport, useMonthlyTrend } from "@/hooks/useReports";
import { useTransactions } from "@/hooks/useTransactions";
import { useUpcomingPayments } from "@/hooks/useRecurringExpenses";
import { usePlannedVsActual } from "@/hooks/useCalendar";
import {
  formatDate,
  formatDaysUntil,
  formatMoney,
  formatTotal,
  formatTotalDeviation,
  MONTH_LABELS,
} from "@/utils/format";

const PIE_COLORS = ["#1F6FEB", "#00B894", "#F5A623", "#E5484D", "#8E5CF7", "#00A8CC", "#FF7A85"];

export function DashboardPage() {
  const theme = useTheme();
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;

  const monthStart = useMemo(
    () => `${year}-${String(month).padStart(2, "0")}-01`,
    [year, month]
  );
  const monthEnd = useMemo(() => {
    const lastDay = new Date(year, month, 0).getDate();
    return `${year}-${String(month).padStart(2, "0")}-${String(lastDay).padStart(2, "0")}`;
  }, [year, month]);

  const accountsQuery = useAccounts();
  const monthlyQuery = useMonthlyReport(year, month);
  const expensesQuery = useExpensesByCategory(monthStart, monthEnd);
  const trendQuery = useMonthlyTrend(6);
  const recentTxQuery = useTransactions({ page: 0, size: 5 });
  const upcomingQuery = useUpcomingPayments(5);
  const plannedVsActualQuery = usePlannedVsActual(year, month);

  const balancesByCurrency = useMemo(() => {
    const accounts = accountsQuery.data ?? [];
    const map = new Map<string, number>();
    for (const account of accounts) {
      if (account.archived) continue;
      map.set(account.currency, (map.get(account.currency) ?? 0) + account.balance);
    }
    return Array.from(map.entries());
  }, [accountsQuery.data]);

  const pieData = (expensesQuery.data ?? []).map((item) => ({
    name: item.categoryName,
    value: item.amount,
  }));

  const trendData = trendQuery.data.map((point) => ({
    name: `${MONTH_LABELS[point.month - 1].slice(0, 3)}`,
    Доход: point.income,
    Расход: point.expense,
  }));

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h5">Добро пожаловать 👋</Typography>
        <Typography variant="body2" color="text.secondary">
          Обзор ваших финансов за {MONTH_LABELS[month - 1].toLowerCase()} {year}
        </Typography>
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2.5,
          gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", lg: "repeat(4, 1fr)" },
        }}
      >
        <StatCard
          label="Доход за месяц"
          value={monthlyQuery.data ? formatMoney(monthlyQuery.data.income) : "—"}
          icon={<TrendingUpRoundedIcon />}
          color="success"
        />
        <StatCard
          label="Расход за месяц"
          value={monthlyQuery.data ? formatMoney(monthlyQuery.data.expense) : "—"}
          icon={<TrendingDownRoundedIcon />}
          color="error"
        />
        <StatCard
          label="Баланс за месяц"
          value={monthlyQuery.data ? formatMoney(monthlyQuery.data.balance) : "—"}
          icon={<ReceiptLongRoundedIcon />}
          color="primary"
        />
        <Card sx={{ height: "100%" }}>
          <CardContent>
            <Stack direction="row" spacing={2} alignItems="flex-start" justifyContent="space-between">
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Баланс по счетам
                </Typography>
                {balancesByCurrency.length === 0 ? (
                  <Typography variant="h5">—</Typography>
                ) : (
                  <Stack spacing={0.5}>
                    {balancesByCurrency.map(([currency, value]) => (
                      <Typography key={currency} variant="h6" sx={{ wordBreak: "break-word" }}>
                        {formatMoney(value, currency as never)}
                      </Typography>
                    ))}
                  </Stack>
                )}
              </Box>
              <Box
                sx={{
                  bgcolor: (t) => `${t.palette.secondary.main}1F`,
                  color: "secondary.main",
                  borderRadius: "50%",
                  width: 44,
                  height: 44,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  flexShrink: 0,
                }}
              >
                <AccountBalanceWalletRoundedIcon />
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2.5,
          gridTemplateColumns: { xs: "1fr", lg: "7fr 5fr" },
        }}
      >
        <Card>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Доходы и расходы за 6 месяцев
            </Typography>
            {trendQuery.isLoading ? (
              <LoadingState minHeight={300} />
            ) : (
              <Box sx={{ width: "100%", height: 300 }}>
                <ResponsiveContainer>
                  <BarChart data={trendData}>
                    <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
                    <XAxis dataKey="name" stroke={theme.palette.text.secondary} fontSize={12} />
                    <YAxis stroke={theme.palette.text.secondary} fontSize={12} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: theme.palette.background.paper,
                        border: `1px solid ${theme.palette.divider}`,
                        borderRadius: 8,
                      }}
                    />
                    <Legend />
                    <Bar dataKey="Доход" fill={theme.palette.success.main} radius={[6, 6, 0, 0]} />
                    <Bar dataKey="Расход" fill={theme.palette.error.main} radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            )}
          </CardContent>
        </Card>

        <Card sx={{ height: "100%" }}>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Расходы по категориям
            </Typography>
            {expensesQuery.isLoading ? (
              <LoadingState minHeight={300} />
            ) : pieData.length === 0 ? (
              <EmptyState title="Нет расходов в этом месяце" />
            ) : (
              <Box sx={{ width: "100%", height: 300 }}>
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={pieData}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={60}
                      outerRadius={95}
                      paddingAngle={2}
                    >
                      {pieData.map((_, index) => (
                        <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value: number) => formatMoney(value)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            )}
          </CardContent>
        </Card>
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2.5,
          gridTemplateColumns: { xs: "1fr", lg: "repeat(2, 1fr)" },
        }}
      >
        <Card>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Счета
            </Typography>
            {accountsQuery.isLoading ? (
              <LoadingState minHeight={160} />
            ) : accountsQuery.isError ? (
              <ErrorState error={accountsQuery.error} onRetry={() => accountsQuery.refetch()} />
            ) : accountsQuery.data && accountsQuery.data.length > 0 ? (
              <Stack spacing={1.5}>
                {accountsQuery.data.slice(0, 5).map((account) => (
                  <Stack
                    key={account.id}
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    sx={{ py: 0.75, borderBottom: "1px solid", borderColor: "divider" }}
                  >
                    <Stack direction="row" spacing={1} alignItems="center">
                      <Typography variant="body2">{account.name}</Typography>
                      {account.archived && <Chip label="Архив" size="small" />}
                    </Stack>
                    <Typography variant="body2" fontWeight={600}>
                      {formatMoney(account.balance, account.currency)}
                    </Typography>
                  </Stack>
                ))}
                <Typography
                  component={RouterLink}
                  to="/accounts"
                  variant="body2"
                  color="primary"
                  sx={{ textDecoration: "none", mt: 1 }}
                >
                  Все счета →
                </Typography>
              </Stack>
            ) : (
              <EmptyState title="Пока нет счетов" description="Добавьте первый счёт, чтобы начать учёт" />
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Последние транзакции
            </Typography>
            {recentTxQuery.isLoading ? (
              <LoadingState minHeight={160} />
            ) : recentTxQuery.isError ? (
              <ErrorState error={recentTxQuery.error} onRetry={() => recentTxQuery.refetch()} />
            ) : recentTxQuery.data && recentTxQuery.data.content.length > 0 ? (
              <Stack spacing={1.5}>
                {recentTxQuery.data.content.map((tx) => (
                  <Stack
                    key={tx.id}
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    sx={{ py: 0.75, borderBottom: "1px solid", borderColor: "divider" }}
                  >
                    <Box sx={{ minWidth: 0 }}>
                      <Typography variant="body2" noWrap>
                        {tx.categoryName} · {tx.accountName}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatDate(tx.transactionDate)}
                      </Typography>
                    </Box>
                    <Typography
                      variant="body2"
                      fontWeight={600}
                      color={tx.type === "INCOME" ? "success.main" : "error.main"}
                    >
                      {tx.type === "INCOME" ? "+" : "−"}
                      {formatMoney(tx.amount, tx.currency)}
                    </Typography>
                  </Stack>
                ))}
                <Typography
                  component={RouterLink}
                  to="/transactions"
                  variant="body2"
                  color="primary"
                  sx={{ textDecoration: "none", mt: 1 }}
                >
                  Все транзакции →
                </Typography>
              </Stack>
            ) : (
              <EmptyState title="Пока нет транзакций" />
            )}
          </CardContent>
        </Card>
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2.5,
          gridTemplateColumns: { xs: "1fr", lg: "7fr 5fr" },
        }}
      >
        <Card>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Ближайшие платежи
            </Typography>
            {upcomingQuery.isLoading ? (
              <LoadingState minHeight={160} />
            ) : upcomingQuery.isError ? (
              <ErrorState error={upcomingQuery.error} onRetry={() => upcomingQuery.refetch()} />
            ) : upcomingQuery.data && upcomingQuery.data.length > 0 ? (
              <Stack spacing={1.5}>
                {upcomingQuery.data.map((payment) => (
                  <Stack
                    key={payment.occurrenceId}
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    sx={{ py: 0.75, borderBottom: "1px solid", borderColor: "divider" }}
                  >
                    <Box sx={{ minWidth: 0 }}>
                      <Typography variant="body2" noWrap>
                        {payment.name}
                      </Typography>
                      <Typography
                        variant="caption"
                        color={payment.overdue ? "error.main" : "text.secondary"}
                      >
                        {formatDaysUntil(payment.daysUntil)} · {formatDate(payment.plannedDate)}
                      </Typography>
                    </Box>
                    <Typography variant="body2" fontWeight={600}>
                      {formatMoney(payment.plannedAmount, payment.currency)}
                    </Typography>
                  </Stack>
                ))}
                <Typography
                  component={RouterLink}
                  to="/calendar"
                  variant="body2"
                  color="primary"
                  sx={{ textDecoration: "none", mt: 1 }}
                >
                  Открыть календарь →
                </Typography>
              </Stack>
            ) : (
              <EmptyState
                icon={<EventRepeatRoundedIcon fontSize="large" color="disabled" />}
                title="Нет запланированных платежей"
                description="Добавьте регулярный расход, чтобы видеть предстоящие списания"
              />
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              План против факта
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {MONTH_LABELS[month - 1]} {year}
            </Typography>

            {plannedVsActualQuery.isLoading ? (
              <LoadingState minHeight={160} />
            ) : plannedVsActualQuery.isError ? (
              <ErrorState
                error={plannedVsActualQuery.error}
                onRetry={() => plannedVsActualQuery.refetch()}
              />
            ) : (
              <Stack spacing={2} sx={{ mt: 2 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="baseline">
                  <Typography variant="body2" color="text.secondary">
                    Запланировано
                  </Typography>
                  <Typography variant="h6">
                    {formatTotal(
                      plannedVsActualQuery.data?.plannedTotal ?? 0,
                      plannedVsActualQuery.data?.currency ?? null
                    )}
                  </Typography>
                </Stack>
                <Stack direction="row" justifyContent="space-between" alignItems="baseline">
                  <Typography variant="body2" color="text.secondary">
                    Фактически
                  </Typography>
                  <Typography variant="h6">
                    {formatTotal(
                      plannedVsActualQuery.data?.actualTotal ?? 0,
                      plannedVsActualQuery.data?.currency ?? null
                    )}
                  </Typography>
                </Stack>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="baseline"
                  sx={{ pt: 1, borderTop: "1px solid", borderColor: "divider" }}
                >
                  <Typography variant="body2" color="text.secondary">
                    Отклонение
                  </Typography>
                  <Typography
                    variant="h6"
                    color={
                      (plannedVsActualQuery.data?.deviation ?? 0) > 0
                        ? "error.main"
                        : (plannedVsActualQuery.data?.deviation ?? 0) < 0
                          ? "success.main"
                          : "text.primary"
                    }
                  >
                    {formatTotalDeviation(
                      plannedVsActualQuery.data?.deviation ?? 0,
                      plannedVsActualQuery.data?.currency ?? null
                    )}
                  </Typography>
                </Stack>
              </Stack>
            )}
          </CardContent>
        </Card>
      </Box>
    </Stack>
  );
}
