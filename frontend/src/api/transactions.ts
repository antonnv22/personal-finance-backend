import { apiClient } from "./client";
import type {
  CreateTransactionRequest,
  Page,
  TransactionFilters,
  TransactionResponse,
} from "./types";

export const transactionsApi = {
  getAll: (filters: TransactionFilters) =>
    apiClient
      .get<Page<TransactionResponse>>("/transactions", { params: filters })
      .then((res) => res.data),

  getById: (id: string) =>
    apiClient.get<TransactionResponse>(`/transactions/${id}`).then((res) => res.data),

  create: (payload: CreateTransactionRequest) =>
    apiClient.post<TransactionResponse>("/transactions", payload).then((res) => res.data),

  remove: (id: string) => apiClient.delete<void>(`/transactions/${id}`).then((res) => res.data),
};
