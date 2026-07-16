import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiError, getFieldErrors } from '@/lib/api';
import { applyFieldErrors } from '@/lib/formErrors';
import { useToast } from '@/components/ui/toast';
import { Dialog } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { TextField } from '@/components/auth/TextField';
import { ACCOUNT_TYPE_OPTIONS, accountIcon } from '@/lib/finance';
import { COMMON_CURRENCIES, accountSchema, type AccountValues } from '@/lib/validations';
import { useCreateAccount, useUpdateAccount } from '@/hooks/useAccounts';
import type { Account } from '@/types';
import { cn } from '@/lib/utils';

interface AccountFormDialogProps {
  open: boolean;
  onClose: () => void;
  account?: Account | null;
}

export function AccountFormDialog({ open, onClose, account }: AccountFormDialogProps) {
  const { toast } = useToast();
  const isEdit = Boolean(account);
  const createAccount = useCreateAccount();
  const updateAccount = useUpdateAccount();

  const form = useForm<AccountValues>({
    resolver: zodResolver(accountSchema),
    defaultValues: {
      name: account?.name ?? '',
      type: account?.type ?? 'CHECKING',
      currency: account?.currency ?? 'USD',
      balance: account ? account.balance : 0,
      institution: account?.institution ?? '',
      color: account?.color ?? '#60A5FA',
      icon: account?.icon ?? '',
      active: account?.active ?? true,
    },
  });

  const watchedType = form.watch('type');
  const TypeIcon = accountIcon(watchedType ?? 'CHECKING');

  const onSubmit = form.handleSubmit(async (values) => {
    const base = {
      name: values.name.trim(),
      type: values.type,
      currency: values.currency.toUpperCase(),
      balance: Number(values.balance ?? 0),
      institution: values.institution?.trim() || undefined,
      color: values.color || undefined,
      icon: values.icon?.trim() || undefined,
    };
    try {
      if (isEdit && account) {
        await updateAccount.mutateAsync({
          id: account.id,
          payload: { ...base, active: values.active },
        });
        toast({ title: 'Account updated', tone: 'success' });
      } else {
        await createAccount.mutateAsync(base);
        toast({ title: 'Account created', tone: 'success' });
      }
      onClose();
    } catch (error) {
      applyFieldErrors(form.setError, error, [
        'name',
        'type',
        'currency',
        'balance',
        'institution',
        'color',
        'icon',
      ]);
      if (getFieldErrors(error).length === 0) {
        toast({
          title: isEdit ? 'Could not update account' : 'Could not create account',
          description: error instanceof ApiError ? error.message : 'Please try again.',
          tone: 'danger',
        });
      }
    }
  });

  const pending = createAccount.isPending || updateAccount.isPending;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit account' : 'Add account'}
      description={
        isEdit
          ? 'Update the details or deactivate this account.'
          : 'Track a bank, card, wallet, or cash you want to manage.'
      }
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="outline" onClick={onClose} disabled={pending} type="button">
            Cancel
          </Button>
          <Button type="button" onClick={onSubmit} disabled={pending}>
            {pending ? 'Saving…' : isEdit ? 'Save changes' : 'Create account'}
          </Button>
        </div>
      }
    >
      <form className="flex flex-col gap-4" onSubmit={onSubmit} noValidate>
        <div className="flex items-center gap-3 rounded-lg border border-border bg-surface-2 px-3 py-2">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/15 text-primary">
            <TypeIcon className="h-5 w-5" aria-hidden="true" />
          </div>
          <div>
            <p className="text-sm font-medium">{watchedType ? watchedType.replace('_', ' ') : 'Account'}</p>
            <p className="text-xs text-muted-foreground">Icon is set by account type</p>
          </div>
        </div>

        <TextField
          label="Account name"
          placeholder="Everyday checking"
          error={form.formState.errors.name?.message}
          {...form.register('name')}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select
            label="Type"
            options={ACCOUNT_TYPE_OPTIONS}
            error={form.formState.errors.type?.message}
            {...form.register('type')}
          />
          <Select
            label="Currency"
            options={COMMON_CURRENCIES.map((currency) => ({ value: currency, label: currency }))}
            error={form.formState.errors.currency?.message}
            {...form.register('currency')}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <TextField
            label="Starting balance"
            type="number"
            step="0.01"
            placeholder="0.00"
            error={form.formState.errors.balance?.message}
            {...form.register('balance')}
          />
          <TextField
            label="Institution"
            placeholder="Nova Bank"
            error={form.formState.errors.institution?.message}
            {...form.register('institution')}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="account-color" className="text-sm font-medium text-foreground">
            Color
          </label>
          <div className="flex items-center gap-3">
            <input
              id="account-color"
              type="color"
              className={cn(
                'h-10 w-14 cursor-pointer rounded-md border border-border bg-surface',
                'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/70',
              )}
              {...form.register('color')}
            />
            <span className="text-sm text-muted-foreground">Used to color this account across Nova</span>
          </div>
        </div>

        {isEdit ? (
          <label className="flex items-center gap-2 text-sm text-foreground">
            <input type="checkbox" className="h-4 w-4 accent-primary" {...form.register('active')} />
            Account is active
          </label>
        ) : null}
      </form>
    </Dialog>
  );
}
