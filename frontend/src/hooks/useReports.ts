import { useQueries, useQuery } from "@tanstack/react-query";
import { reportsApi } from "@/api/reports";

export function useMonthlyReport(year: number, month: number) {
  return useQuery({
    queryKey: ["reports", "monthly", year, month],
    queryFn: () => reportsApi.getMonthly(year, month),
  });
}

export function useExpensesByCategory(from: string, to: string) {
  return useQuery({
    queryKey: ["reports", "expenses-by-category", from, to],
    queryFn: () => reportsApi.getExpensesByCategory(from, to),
    enabled: Boolean(from && to),
  });
}

export function useSummaryReport(from: string, to: string) {
  return useQuery({
    queryKey: ["reports", "summary", from, to],
    queryFn: () => reportsApi.getSummary(from, to),
    enabled: Boolean(from && to),
  });
}

export interface MonthPoint {
  year: number;
  month: number;
  income: number;
  expense: number;
  balance: number;
}

/** Client-side aggregated trend: the backend only exposes a single-month report,
 * so the dashboard fetches the last `count` months in parallel. */
export function useMonthlyTrend(count = 6) {
  const now = new Date();
  const months = Array.from({ length: count }, (_, i) => {
    const d = new Date(now.getFullYear(), now.getMonth() - (count - 1 - i), 1);
    return { year: d.getFullYear(), month: d.getMonth() + 1 };
  });

  const results = useQueries({
    queries: months.map(({ year, month }) => ({
      queryKey: ["reports", "monthly", year, month],
      queryFn: () => reportsApi.getMonthly(year, month),
    })),
  });

  const isLoading = results.some((r) => r.isLoading);
  const isError = results.some((r) => r.isError);
  const data: MonthPoint[] = results
    .map((r) => r.data)
    .filter((d): d is NonNullable<typeof d> => Boolean(d))
    .map((d) => ({
      year: d.year,
      month: d.month,
      income: d.income,
      expense: d.expense,
      balance: d.balance,
    }));

  return { data, isLoading, isError };
}
