import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { accountsApi } from '@/lib/api';
import type { Account, CreateAccountPayload, UpdateAccountPayload } from '@/types';

const ACCOUNTS_KEY = ['accounts'] as const;

/** Loads the authenticated user's accounts. */
export function useAccounts() {
  return useQuery<Account[]>({
    queryKey: ACCOUNTS_KEY,
    queryFn: () => accountsApi.list(),
  });
}

function useInvalidateAccounts() {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY });
    queryClient.invalidateQueries({ queryKey: ['dashboard'] });
  };
}

export function useCreateAccount() {
  const invalidate = useInvalidateAccounts();
  return useMutation({
    mutationFn: (payload: CreateAccountPayload) => accountsApi.create(payload),
    onSuccess: invalidate,
  });
}

export function useUpdateAccount() {
  const invalidate = useInvalidateAccounts();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateAccountPayload }) =>
      accountsApi.update(id, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteAccount() {
  const invalidate = useInvalidateAccounts();
  return useMutation({
    mutationFn: (id: string) => accountsApi.remove(id),
    onSuccess: invalidate,
  });
}
