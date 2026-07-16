import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { transactionsApi } from '@/lib/api';
import type {
  CreateTransactionPayload,
  Transaction,
  TransactionQuery,
  UpdateTransactionPayload,
} from '@/types';

/** Loads transactions matching the given filter. The filter is part of the key. */
export function useTransactions(query: TransactionQuery = {}) {
  return useQuery<Transaction[]>({
    queryKey: ['transactions', query],
    queryFn: () => transactionsApi.list(query),
  });
}

/** Loads a single transaction by id, for the edit form. */
export function useTransaction(id: string | undefined) {
  return useQuery<Transaction>({
    queryKey: ['transaction', id],
    queryFn: () => transactionsApi.get(id as string),
    enabled: Boolean(id),
  });
}

function useInvalidateTransactions() {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: ['transactions'] });
    queryClient.invalidateQueries({ queryKey: ['accounts'] });
    queryClient.invalidateQueries({ queryKey: ['dashboard'] });
  };
}

export function useCreateTransaction() {
  const invalidate = useInvalidateTransactions();
  return useMutation({
    mutationFn: (payload: CreateTransactionPayload) => transactionsApi.create(payload),
    onSuccess: invalidate,
  });
}

export function useUpdateTransaction() {
  const invalidate = useInvalidateTransactions();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateTransactionPayload }) =>
      transactionsApi.update(id, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteTransaction() {
  const invalidate = useInvalidateTransactions();
  return useMutation({
    mutationFn: (id: string) => transactionsApi.remove(id),
    onSuccess: invalidate,
  });
}
