import { apiClient } from "./client";
import type {
  CategoryResponse,
  CategoryType,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "./types";

export const categoriesApi = {
  getAll: (type?: CategoryType) =>
    apiClient
      .get<CategoryResponse[]>("/categories", { params: type ? { type } : undefined })
      .then((res) => res.data),

  create: (payload: CreateCategoryRequest) =>
    apiClient.post<CategoryResponse>("/categories", payload).then((res) => res.data),

  update: (id: string, payload: UpdateCategoryRequest) =>
    apiClient.put<CategoryResponse>(`/categories/${id}`, payload).then((res) => res.data),
};
