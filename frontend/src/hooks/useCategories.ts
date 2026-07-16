import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { categoriesApi } from '@/lib/api';
import type { Category, CreateCategoryPayload, UpdateCategoryPayload } from '@/types';

const CATEGORIES_KEY = ['categories'] as const;

/** Loads the authenticated user's categories (income and expense). */
export function useCategories() {
  return useQuery<Category[]>({
    queryKey: CATEGORIES_KEY,
    queryFn: () => categoriesApi.list(),
  });
}

function useInvalidateCategories() {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY });
    queryClient.invalidateQueries({ queryKey: ['dashboard'] });
  };
}

export function useCreateCategory() {
  const invalidate = useInvalidateCategories();
  return useMutation({
    mutationFn: (payload: CreateCategoryPayload) => categoriesApi.create(payload),
    onSuccess: invalidate,
  });
}

export function useUpdateCategory() {
  const invalidate = useInvalidateCategories();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateCategoryPayload }) =>
      categoriesApi.update(id, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteCategory() {
  const invalidate = useInvalidateCategories();
  return useMutation({
    mutationFn: (id: string) => categoriesApi.remove(id),
    onSuccess: invalidate,
  });
}
