import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { categoriesApi } from "@/api/categories";
import type { CategoryType, CreateCategoryRequest, UpdateCategoryRequest } from "@/api/types";

const categoriesKey = (type?: CategoryType) => ["categories", type ?? "all"] as const;

export function useCategories(type?: CategoryType) {
  return useQuery({
    queryKey: categoriesKey(type),
    queryFn: () => categoriesApi.getAll(type),
  });
}

export function useCreateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateCategoryRequest) => categoriesApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["categories"] }),
  });
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateCategoryRequest }) =>
      categoriesApi.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["categories"] }),
  });
}
