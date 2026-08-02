import { apiClient } from "./client";
import type {
  CalendarItemResponse,
  CompleteOccurrenceRequest,
  CreateRecurringExpenseRequest,
  RecurringExpenseResponse,
  UpcomingPaymentResponse,
  UpdateRecurringExpenseRequest,
} from "./types";

export const recurringExpensesApi = {
  getAll: () =>
    apiClient.get<RecurringExpenseResponse[]>("/recurring-expenses").then((res) => res.data),

  create: (payload: CreateRecurringExpenseRequest) =>
    apiClient.post<RecurringExpenseResponse>("/recurring-expenses", payload).then((res) => res.data),

  update: (id: string, payload: UpdateRecurringExpenseRequest) =>
    apiClient
      .put<RecurringExpenseResponse>(`/recurring-expenses/${id}`, payload)
      .then((res) => res.data),

  remove: (id: string) =>
    apiClient.delete<void>(`/recurring-expenses/${id}`).then((res) => res.data),

  getUpcoming: (limit = 5) =>
    apiClient
      .get<UpcomingPaymentResponse[]>("/recurring-expenses/upcoming", { params: { limit } })
      .then((res) => res.data),

  completeOccurrence: (occurrenceId: string, payload: CompleteOccurrenceRequest) =>
    apiClient
      .post<CalendarItemResponse>(`/recurring-expenses/occurrences/${occurrenceId}/complete`, payload)
      .then((res) => res.data),

  skipOccurrence: (occurrenceId: string) =>
    apiClient
      .post<CalendarItemResponse>(`/recurring-expenses/occurrences/${occurrenceId}/skip`)
      .then((res) => res.data),
};
