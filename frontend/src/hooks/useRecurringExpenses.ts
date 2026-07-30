import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { recurringExpensesApi } from "@/api/recurringExpenses";
import type {
  CompleteOccurrenceRequest,
  CreateRecurringExpenseRequest,
  UpdateRecurringExpenseRequest,
} from "@/api/types";

const RULES_KEY = ["recurring-expenses"] as const;

/**
 * Подтверждение платежа создаёт настоящую транзакцию, поэтому вместе с
 * календарём устаревают баланс счетов, список транзакций и отчёты.
 */
function useInvalidateAfterPaymentChange() {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: ["calendar"] });
    queryClient.invalidateQueries({ queryKey: RULES_KEY });
    queryClient.invalidateQueries({ queryKey: ["upcoming-payments"] });
    queryClient.invalidateQueries({ queryKey: ["transactions"] });
    queryClient.invalidateQueries({ queryKey: ["accounts"] });
    queryClient.invalidateQueries({ queryKey: ["reports"] });
  };
}

export function useRecurringExpenses() {
  return useQuery({
    queryKey: RULES_KEY,
    queryFn: recurringExpensesApi.getAll,
  });
}

export function useUpcomingPayments(limit = 5) {
  return useQuery({
    queryKey: ["upcoming-payments", limit],
    queryFn: () => recurringExpensesApi.getUpcoming(limit),
  });
}

export function useCreateRecurringExpense() {
  const invalidate = useInvalidateAfterPaymentChange();
  return useMutation({
    mutationFn: (payload: CreateRecurringExpenseRequest) => recurringExpensesApi.create(payload),
    onSuccess: invalidate,
  });
}

export function useUpdateRecurringExpense() {
  const invalidate = useInvalidateAfterPaymentChange();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateRecurringExpenseRequest }) =>
      recurringExpensesApi.update(id, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteRecurringExpense() {
  const invalidate = useInvalidateAfterPaymentChange();
  return useMutation({
    mutationFn: (id: string) => recurringExpensesApi.remove(id),
    onSuccess: invalidate,
  });
}

export function useCompleteOccurrence() {
  const invalidate = useInvalidateAfterPaymentChange();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CompleteOccurrenceRequest }) =>
      recurringExpensesApi.completeOccurrence(id, payload),
    onSuccess: invalidate,
  });
}

export function useSkipOccurrence() {
  const invalidate = useInvalidateAfterPaymentChange();
  return useMutation({
    mutationFn: (id: string) => recurringExpensesApi.skipOccurrence(id),
    onSuccess: invalidate,
  });
}
