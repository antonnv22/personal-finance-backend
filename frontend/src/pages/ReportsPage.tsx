import { useMemo, useState } from "react";
import {
  Box,
  Card,
  CardContent,
  LinearProgress,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
  useTheme,
} from "@mui/material";
import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import TrendingUpRoundedIcon from "@mui/icons-material/TrendingUpRounded";
import TrendingDownRoundedIcon from "@mui/icons-material/TrendingDownRounded";
import AccountBalanceRoundedIcon from "@mui/icons-material/AccountBalanceRounded";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { StatCard } from "@/components/common/StatCard";
import { useExpensesByCategory, useMonthlyReport, useSummaryReport } from "@/hooks/useReports";
import { formatMoney, MONTH_LABELS, todayIso } from "@/utils/format";

const PIE_COLORS = ["#1F6FEB", "#00B894", "#F5A623", "#E5484D", "#8E5CF7", "#00A8CC", "#FF7A85"];

function firstDayOfMonthIso(monthsAgo: number): string {
  const d = new Date();
  d.setDate(1);
  d.setMonth(d.getMonth() - monthsAgo);
  return d.toISOString().slice(0, 10);
}

export function ReportsPage() {
  const theme = useTheme();
  const now = new Date();

  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const [from, setFrom] = useState(firstDayOfMonthIso(2));
  const [to, setTo] = useState(todayIso());

  const currentYear = now.getFullYear();
  const yearOptions = useMemo(
    () => Array.from({ length: 6 }, (_, i) => currentYear - i),
    [currentYear]
  );

  const monthlyQuery = useMonthlyReport(year, month);
  const summaryQuery = useSummaryReport(from, to);
  const expensesQuery = useExpensesByCategory(from, to);

  const totalExpense = (expensesQuery.data ?? []).reduce((sum, item) => sum + item.amount, 0);
  const pieData = (expensesQuery.data ?? []).map((item) => ({ name: item.categoryName, value: item.amount }));

  return (
    <Stack spacing={3}>
      <PageHeader title="Отчёты" subtitle="Анализируйте доходы и расходы за выбранный период" />

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Отчёт за месяц
          </Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 2.5 }}>
            <TextField
              select
              label="Месяц"
              size="small"
              value={month}
              onChange={(e) => setMonth(Number(e.target.value))}
              sx={{ minWidth: 160 }}
            >
              {MONTH_LABELS.map((label, index) => (
                <MenuItem key={label} value={index + 1}>
                  {label}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Год"
              size="small"
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
              sx={{ minWidth: 120 }}
            >
              {yearOptions.map((y) => (
                <MenuItem key={y} value={y}>
                  {y}
                </MenuItem>
              ))}
            </TextField>
          </Stack>

          {monthlyQuery.isLoading ? (
            <LoadingState minHeight={120} />
          ) : monthlyQuery.isError ? (
            <ErrorState error={monthlyQuery.error} onRetry={() => monthlyQuery.refetch()} />
          ) : (
            <Box
              sx={{
                display: "grid",
                gap: 2,
                gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
              }}
            >
              <StatCard
                label="Доход"
                value={formatMoney(monthlyQuery.data?.income ?? 0)}
                icon={<TrendingUpRoundedIcon />}
                color="success"
              />
              <StatCard
                label="Расход"
                value={formatMoney(monthlyQuery.data?.expense ?? 0)}
                icon={<TrendingDownRoundedIcon />}
                color="error"
              />
              <StatCard
                label="Баланс"
                value={formatMoney(monthlyQuery.data?.balance ?? 0)}
                icon={<AccountBalanceRoundedIcon />}
                color="primary"
              />
            </Box>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Отчёт за период
          </Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 2.5 }}>
            <TextField
              label="С даты"
              type="date"
              size="small"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label="По дату"
              type="date"
              size="small"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>

          {summaryQuery.isLoading ? (
            <LoadingState minHeight={120} />
          ) : summaryQuery.isError ? (
            <ErrorState error={summaryQuery.error} onRetry={() => summaryQuery.refetch()} />
          ) : (
            <Box
              sx={{
                display: "grid",
                gap: 2,
                gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
                mb: 3,
              }}
            >
              <StatCard
                label="Доход"
                value={formatMoney(summaryQuery.data?.income ?? 0)}
                icon={<TrendingUpRoundedIcon />}
                color="success"
              />
              <StatCard
                label="Расход"
                value={formatMoney(summaryQuery.data?.expense ?? 0)}
                icon={<TrendingDownRoundedIcon />}
                color="error"
              />
              <StatCard
                label="Баланс"
                value={formatMoney(summaryQuery.data?.balance ?? 0)}
                icon={<AccountBalanceRoundedIcon />}
                color="primary"
              />
            </Box>
          )}

          <Typography variant="subtitle1" gutterBottom>
            Расходы по категориям
          </Typography>

          {expensesQuery.isLoading ? (
            <LoadingState minHeight={200} />
          ) : pieData.length === 0 ? (
            <EmptyState title="Нет расходов за выбранный период" />
          ) : (
            <Box
              sx={{
                display: "grid",
                gap: 2.5,
                gridTemplateColumns: { xs: "1fr", md: "5fr 7fr" },
                alignItems: "center",
              }}
            >
              <Box sx={{ width: "100%", height: 260 }}>
                <ResponsiveContainer>
                  <PieChart>
                    <Pie data={pieData} dataKey="value" nameKey="name" innerRadius={55} outerRadius={90} paddingAngle={2}>
                      {pieData.map((_, index) => (
                        <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value: number) => formatMoney(value)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </Box>

              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Категория</TableCell>
                    <TableCell align="right">Сумма</TableCell>
                    <TableCell sx={{ width: "35%" }}>Доля</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(expensesQuery.data ?? []).map((item, index) => {
                    const share = totalExpense > 0 ? (item.amount / totalExpense) * 100 : 0;
                    return (
                      <TableRow key={item.categoryName}>
                        <TableCell>{item.categoryName}</TableCell>
                        <TableCell align="right">{formatMoney(item.amount)}</TableCell>
                        <TableCell>
                          <Stack direction="row" spacing={1} alignItems="center">
                            <LinearProgress
                              variant="determinate"
                              value={share}
                              sx={{
                                flexGrow: 1,
                                height: 6,
                                borderRadius: 3,
                                bgcolor: theme.palette.action.hover,
                                "& .MuiLinearProgress-bar": {
                                  bgcolor: PIE_COLORS[index % PIE_COLORS.length],
                                },
                              }}
                            />
                            <Typography variant="caption" color="text.secondary" sx={{ minWidth: 36 }}>
                              {share.toFixed(0)}%
                            </Typography>
                          </Stack>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </Box>
          )}
        </CardContent>
      </Card>
    </Stack>
  );
}
