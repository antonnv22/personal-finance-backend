import { apiClient } from "./client";
import type { AccountResponse, CreateAccountRequest, UpdateAccountRequest } from "./types";

export const accountsApi = {
  getAll: () => apiClient.get<AccountResponse[]>("/accounts").then((res) => res.data),

  getById: (id: string) =>
    apiClient.get<AccountResponse>(`/accounts/${id}`).then((res) => res.data),

  create: (payload: CreateAccountRequest) =>
    apiClient.post<AccountResponse>("/accounts", payload).then((res) => res.data),

  update: (id: string, payload: UpdateAccountRequest) =>
    apiClient.put<AccountResponse>(`/accounts/${id}`, payload).then((res) => res.data),

  remove: (id: string) => apiClient.delete<void>(`/accounts/${id}`).then((res) => res.data),
};
