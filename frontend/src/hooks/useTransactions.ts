import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { transactionsApi } from "@/api/transactions";
import type { CreateTransactionRequest, TransactionFilters } from "@/api/types";

export function useTransactions(filters: TransactionFilters) {
  return useQuery({
    queryKey: ["transactions", filters],
    queryFn: () => transactionsApi.getAll(filters),
    placeholderData: (previous) => previous,
  });
}

export function useCreateTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateTransactionRequest) => transactionsApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      queryClient.invalidateQueries({ queryKey: ["reports"] });
    },
  });
}

export function useDeleteTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => transactionsApi.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      queryClient.invalidateQueries({ queryKey: ["reports"] });
    },
  });
}
