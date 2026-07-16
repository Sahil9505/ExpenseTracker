import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { KeyRound, Mail, ShieldCheck } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingState } from '@/components/ui/loading-state';
import { TextField } from '@/components/auth/TextField';
import { useToast } from '@/components/ui/toast';
import { useAuth } from '@/context/AuthProvider';
import { useCurrentUser } from '@/hooks/useCurrentUser';
import { ApiError, api, getFieldErrors } from '@/lib/api';
import { applyFieldErrors } from '@/lib/formErrors';
import { getInitials } from '@/lib/utils';
import {
  changePasswordSchema,
  COMMON_CURRENCIES,
  profileSchema,
  type ChangePasswordValues,
  type ProfileValues,
} from '@/lib/validations';
import type { CurrentUser } from '@/types';

const TIMEZONES = [
  'America/Los_Angeles',
  'America/Denver',
  'America/Chicago',
  'America/New_York',
  'America/Sao_Paulo',
  'Europe/London',
  'Europe/Paris',
  'Europe/Berlin',
  'Africa/Johannesburg',
  'Asia/Dubai',
  'Asia/Kolkata',
  'Asia/Singapore',
  'Asia/Tokyo',
  'Australia/Sydney',
  'UTC',
];

const selectClass =
  'flex h-10 w-full rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/70 focus-visible:ring-offset-2 ' +
  'focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50';

function toProfilePayload(values: ProfileValues): Record<string, string> {
  const payload: Record<string, string> = {};
  if (values.fullName?.trim()) payload.fullName = values.fullName.trim();
  if (values.preferredCurrency) payload.preferredCurrency = values.preferredCurrency;
  if (values.timezone?.trim()) payload.timezone = values.timezone.trim();
  if (values.avatarUrl?.trim()) payload.avatarUrl = values.avatarUrl.trim();
  return payload;
}

export function Profile() {
  const { setUser } = useAuth();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const query = useCurrentUser();
  const user = query.data;

  const profileForm = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: { fullName: '', preferredCurrency: undefined, timezone: '', avatarUrl: '' },
  });

  const passwordForm = useForm<ChangePasswordValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmNewPassword: '' },
  });

  useEffect(() => {
    if (user) {
      profileForm.reset({
        fullName: user.fullName ?? '',
        preferredCurrency: user.preferredCurrency as ProfileValues['preferredCurrency'],
        timezone: user.timezone ?? '',
        avatarUrl: user.avatarUrl ?? '',
      });
    }
  }, [user, profileForm]);

  const updateProfile = useMutation({
    mutationFn: (values: ProfileValues) =>
      api.patch<CurrentUser>('/api/users/me', toProfilePayload(values)),
    onSuccess: (updated) => {
      queryClient.setQueryData(['currentUser'], updated);
      setUser(updated);
      toast({ title: 'Profile updated', tone: 'success' });
    },
    onError: (error) => {
      applyFieldErrors(profileForm.setError, error, [
        'fullName',
        'preferredCurrency',
        'timezone',
        'avatarUrl',
      ]);
      toast({
        title: 'Could not update profile',
        description: error instanceof ApiError ? error.message : 'Please try again.',
        tone: 'danger',
      });
    },
  });

  const changePassword = useMutation({
    mutationFn: (values: ChangePasswordValues) =>
      api.patch<void>('/api/users/me/password', {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      }),
    onSuccess: () => {
      passwordForm.reset();
      toast({
        title: 'Password changed',
        description: 'Your other sessions were signed out for security.',
        tone: 'success',
      });
    },
    onError: (error) => {
      applyFieldErrors(passwordForm.setError, error, [
        'currentPassword',
        'newPassword',
        'confirmNewPassword',
      ]);
      if (getFieldErrors(error).length === 0) {
        toast({
          title: 'Could not change password',
          description: error instanceof ApiError ? error.message : 'Please try again.',
          tone: 'danger',
        });
      }
    },
  });

  if (query.isError) {
    return (
      <div className="flex flex-col items-start gap-3 rounded-lg border border-border bg-surface p-6">
        <p className="text-sm font-medium">We couldn't load your profile.</p>
        <Button variant="outline" size="sm" onClick={() => query.refetch()}>
          Try again
        </Button>
      </div>
    );
  }

  if (query.isLoading || !user) {
    return <LoadingState label="Loading your profile…" />;
  }

  const avatarUrl = profileForm.watch('avatarUrl');
  const showAvatar = typeof avatarUrl === 'string' && avatarUrl.trim().length > 0;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Profile & settings</h2>
          <p className="text-sm text-muted-foreground">
            Manage how Nova represents you and keeps your account secure.
          </p>
        </div>
        <Badge variant={user.accountStatus === 'ACTIVE' ? 'success' : 'warning'}>
          {user.accountStatus}
        </Badge>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            {showAvatar ? (
              <img
                src={avatarUrl}
                alt=""
                className="h-14 w-14 rounded-full border border-border object-cover"
              />
            ) : (
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-primary to-accent text-lg font-semibold text-primary-foreground">
                {getInitials(user.fullName, user.email)}
              </div>
            )}
            <div className="min-w-0">
              <p className="truncate text-lg font-semibold">{user.fullName || 'Nova user'}</p>
              <p className="flex items-center gap-1.5 truncate text-sm text-muted-foreground">
                <Mail className="h-3.5 w-3.5" />
                {user.email}
              </p>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={profileForm.handleSubmit((values) => updateProfile.mutate(values))}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2"
            noValidate
          >
            <TextField
              label="Full name"
              placeholder="Alex Morgan"
              error={profileForm.formState.errors.fullName?.message}
              {...profileForm.register('fullName')}
            />
            <div className="flex flex-col gap-1.5">
              <label htmlFor="preferredCurrency" className="text-sm font-medium text-foreground">
                Preferred currency
              </label>
              <select
                id="preferredCurrency"
                className={selectClass}
                {...profileForm.register('preferredCurrency')}
              >
                {COMMON_CURRENCIES.map((currency) => (
                  <option key={currency} value={currency}>
                    {currency}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex flex-col gap-1.5">
              <label htmlFor="timezone" className="text-sm font-medium text-foreground">
                Timezone
              </label>
              <select
                id="timezone"
                className={selectClass}
                {...profileForm.register('timezone')}
              >
                <option value="">Auto</option>
                {TIMEZONES.map((zone) => (
                  <option key={zone} value={zone}>
                    {zone}
                  </option>
                ))}
              </select>
            </div>
            <TextField
              label="Avatar URL"
              placeholder="https://…"
              error={profileForm.formState.errors.avatarUrl?.message}
              {...profileForm.register('avatarUrl')}
            />
            <div className="flex items-end sm:col-span-2">
              <Button type="submit" disabled={updateProfile.isPending}>
                {updateProfile.isPending ? 'Saving…' : 'Save changes'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <KeyRound className="h-4 w-4 text-primary" />
            Password
          </CardTitle>
          <CardDescription>
            Use at least 8 characters. Changing your password signs you out of other devices.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={passwordForm.handleSubmit((values) => changePassword.mutate(values))}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2"
            noValidate
          >
            <TextField
              label="Current password"
              type="password"
              autoComplete="current-password"
              error={passwordForm.formState.errors.currentPassword?.message}
              {...passwordForm.register('currentPassword')}
            />
            <div className="hidden sm:block" aria-hidden="true" />
            <TextField
              label="New password"
              type="password"
              autoComplete="new-password"
              error={passwordForm.formState.errors.newPassword?.message}
              {...passwordForm.register('newPassword')}
            />
            <TextField
              label="Confirm new password"
              type="password"
              autoComplete="new-password"
              error={passwordForm.formState.errors.confirmNewPassword?.message}
              {...passwordForm.register('confirmNewPassword')}
            />
            <div className="flex items-end sm:col-span-2">
              <Button type="submit" variant="outline" disabled={changePassword.isPending}>
                {changePassword.isPending ? 'Updating…' : 'Update password'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
        <ShieldCheck className="h-3.5 w-3.5 text-success" />
        Your credentials are hashed with BCrypt and never exposed to the client.
      </p>
    </div>
  );
}
