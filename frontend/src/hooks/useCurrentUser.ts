import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { CurrentUser } from '@/types';

/** Loads the authenticated user's profile. Drives the topbar, settings, and avatar. */
export function useCurrentUser() {
  return useQuery<CurrentUser>({
    queryKey: ['currentUser'],
    queryFn: () => api.get<CurrentUser>('/api/users/me'),
    retry: false,
  });
}
