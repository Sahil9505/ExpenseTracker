import { useState } from 'react';
import { Pencil, Plus, Wallet } from 'lucide-react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { EmptyState } from '@/components/ui/empty-state';
import { Skeleton } from '@/components/ui/loading-state';
import { ConfirmDialog } from '@/components/ui/confirm-dialog';
import { useToast } from '@/components/ui/toast';
import { AccountFormDialog } from '@/components/finance/AccountFormDialog';
import { useAccounts, useDeleteAccount, useUpdateAccount } from '@/hooks/useAccounts';
import { ACCOUNT_TYPE_LABELS, accountIcon, colorOf } from '@/lib/finance';
import { formatCurrency } from '@/lib/utils';
import type { Account } from '@/types';

export function Accounts() {
  const { toast } = useToast();
  const query = useAccounts();
  const deleteAccount = useDeleteAccount();
  const updateAccount = useUpdateAccount();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Account | null>(null);
  const [confirming, setConfirming] = useState<Account | null>(null);

  const openCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const openEdit = (account: Account) => {
    setEditing(account);
    setDialogOpen(true);
  };

  const confirmDeactivate = async () => {
    if (!confirming) return;
    try {
      await deleteAccount.mutateAsync(confirming.id);
      toast({ title: 'Account deactivated', description: 'Its history is kept.', tone: 'success' });
    } catch {
      toast({ title: 'Could not deactivate account', tone: 'danger' });
    } finally {
      setConfirming(null);
    }
  };

  const reactivate = async (account: Account) => {
    try {
      await updateAccount.mutateAsync({ id: account.id, payload: { active: true } });
      toast({ title: 'Account reactivated', tone: 'success' });
    } catch {
      toast({ title: 'Could not reactivate account', tone: 'danger' });
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Accounts</h2>
          <p className="text-sm text-muted-foreground">Your cash, cards, and wallets in one place.</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" aria-hidden="true" />
          Add account
        </Button>
      </div>

      {query.isLoading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} className="h-36 w-full rounded-lg" />
          ))}
        </div>
      ) : query.isError ? (
        <div className="rounded-lg border border-border bg-surface p-6 text-sm">
          <p className="font-medium">We couldn't load your accounts.</p>
          <Button variant="outline" size="sm" className="mt-3" onClick={() => query.refetch()}>
            Try again
          </Button>
        </div>
      ) : query.data && query.data.length === 0 ? (
        <EmptyState
          icon={Wallet}
          title="No accounts yet"
          description="Add your first account to start tracking balances and transactions."
          action={
            <Button onClick={openCreate}>
              <Plus className="h-4 w-4" aria-hidden="true" />
              Add account
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {query.data?.map((account) => {
            const Icon = accountIcon(account.type);
            const color = colorOf(account.color);
            return (
              <Card key={account.id} className="flex flex-col gap-4 p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div
                      className="flex h-11 w-11 items-center justify-center rounded-xl"
                      style={{ backgroundColor: `${color}22`, color }}
                    >
                      <Icon className="h-5 w-5" aria-hidden="true" />
                    </div>
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold">{account.name}</p>
                      <p className="text-xs text-muted-foreground">
                        {ACCOUNT_TYPE_LABELS[account.type]}
                        {account.institution ? ` · ${account.institution}` : ''}
                      </p>
                    </div>
                  </div>
                  {account.active ? (
                    <Badge variant="success">Active</Badge>
                  ) : (
                    <Badge variant="warning">Inactive</Badge>
                  )}
                </div>

                <div>
                  <p className="text-2xl font-bold tabular-nums">
                    {formatCurrency(account.balance, account.currency)}
                  </p>
                  <p className="text-xs text-muted-foreground">{account.currency}</p>
                </div>

                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm" onClick={() => openEdit(account)}>
                    <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
                    Edit
                  </Button>
                  {account.active ? (
                    <Button variant="danger" size="sm" onClick={() => setConfirming(account)}>
                      Deactivate
                    </Button>
                  ) : (
                    <Button variant="outline" size="sm" onClick={() => reactivate(account)}>
                      Reactivate
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      <AccountFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} account={editing} />

      <ConfirmDialog
        open={Boolean(confirming)}
        title="Deactivate this account?"
        description={
          confirming
            ? `${confirming.name} will be hidden from balances and new transactions, but its history is preserved.`
            : undefined
        }
        confirmLabel="Deactivate"
        loading={deleteAccount.isPending}
        onConfirm={confirmDeactivate}
        onClose={() => setConfirming(null)}
      />
    </div>
  );
}
