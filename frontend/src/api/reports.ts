import { apiClient } from "./client";
import type {
  CategoryExpenseResponse,
  MonthlyReportResponse,
  SummaryReportResponse,
} from "./types";

export const reportsApi = {
  getMonthly: (year: number, month: number) =>
    apiClient
      .get<MonthlyReportResponse>("/reports/monthly", { params: { year, month } })
      .then((res) => res.data),

  getExpensesByCategory: (from: string, to: string) =>
    apiClient
      .get<CategoryExpenseResponse[]>("/reports/expenses-by-category", {
        params: { from, to },
      })
      .then((res) => res.data),

  getSummary: (from: string, to: string) =>
    apiClient
      .get<SummaryReportResponse>("/reports/summary", { params: { from, to } })
      .then((res) => res.data),
};
