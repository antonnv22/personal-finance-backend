import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { accountsApi } from "@/api/accounts";
import type { CreateAccountRequest, UpdateAccountRequest } from "@/api/types";

const ACCOUNTS_KEY = ["accounts"] as const;

export function useAccounts() {
  return useQuery({
    queryKey: ACCOUNTS_KEY,
    queryFn: accountsApi.getAll,
  });
}

export function useCreateAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateAccountRequest) => accountsApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY }),
  });
}

export function useUpdateAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateAccountRequest }) =>
      accountsApi.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY }),
  });
}

export function useDeleteAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => accountsApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY }),
  });
}
