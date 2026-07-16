import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiError, getFieldErrors } from '@/lib/api';
import { applyFieldErrors } from '@/lib/formErrors';
import { useToast } from '@/components/ui/toast';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { TextField } from '@/components/auth/TextField';
import { dateInputToIso, isoToDateInput, todayInputValue, TRANSACTION_TYPE_OPTIONS } from '@/lib/finance';
import { transactionSchema, type TransactionValues } from '@/lib/validations';
import { useAccounts } from '@/hooks/useAccounts';
import { useCategories } from '@/hooks/useCategories';
import { useCreateTransaction, useUpdateTransaction } from '@/hooks/useTransactions';
import type { CreateTransactionPayload, Transaction } from '@/types';

interface TransactionFormProps {
  transaction?: Transaction | null;
  onDone: () => void;
}

/**
 * Create/edit form for a single transaction. The visible fields adapt to the
 * chosen type: transfers need a source and destination account (no category),
 * while income and expenses need one account and a matching category.
 */
export function TransactionForm({ transaction, onDone }: TransactionFormProps) {
  const { toast } = useToast();
  const isEdit = Boolean(transaction);
  const createTransaction = useCreateTransaction();
  const updateTransaction = useUpdateTransaction();
  const accounts = useAccounts();
  const categories = useCategories();

  const form = useForm<TransactionValues>({
    resolver: zodResolver(transactionSchema),
    defaultValues: {
      type: transaction?.type ?? 'EXPENSE',
      accountId: transaction?.account?.id ?? '',
      destinationAccountId: transaction?.destinationAccount?.id ?? '',
      categoryId: transaction?.category?.id ?? '',
      amount: transaction ? transaction.amount : 0,
      merchant: transaction?.merchant ?? '',
      note: transaction?.note ?? '',
      occurredAt: transaction ? isoToDateInput(transaction.occurredAt) : todayInputValue(),
    },
  });

  const type = form.watch('type');
  const sourceAccountId = form.watch('accountId');
  const isTransfer = type === 'TRANSFER';

  const accountOptions = (accounts.data ?? []).map((account) => ({
    value: account.id,
    label: account.name,
  }));

  const categoryOptions = (categories.data ?? [])
    .filter((category) => category.type === type)
    .map((category) => ({ value: category.id, label: category.name }));

  const destinationOptions = (accounts.data ?? [])
    .filter((account) => account.id !== sourceAccountId)
    .map((account) => ({ value: account.id, label: account.name }));

  const onSubmit = form.handleSubmit(async (values) => {
    const payload: CreateTransactionPayload = {
      amount: values.amount,
      type: values.type,
      accountId: values.accountId || undefined,
      destinationAccountId: isTransfer ? values.destinationAccountId || undefined : undefined,
      categoryId: isTransfer ? undefined : values.categoryId || undefined,
      merchant: values.merchant?.trim() || undefined,
      note: values.note?.trim() || undefined,
      occurredAt: dateInputToIso(values.occurredAt),
    };
    try {
      if (isEdit && transaction) {
        await updateTransaction.mutateAsync({ id: transaction.id, payload });
        toast({ title: 'Transaction updated', tone: 'success' });
      } else {
        await createTransaction.mutateAsync(payload);
        toast({ title: 'Transaction added', tone: 'success' });
      }
      onDone();
    } catch (error) {
      applyFieldErrors(form.setError, error, [
        'type',
        'accountId',
        'destinationAccountId',
        'categoryId',
        'amount',
        'merchant',
        'note',
        'occurredAt',
      ]);
      if (getFieldErrors(error).length === 0) {
        toast({
          title: isEdit ? 'Could not update transaction' : 'Could not add transaction',
          description: error instanceof ApiError ? error.message : 'Please try again.',
          tone: 'danger',
        });
      }
    }
  });

  const pending = createTransaction.isPending || updateTransaction.isPending;

  return (
    <form className="flex flex-col gap-5" onSubmit={onSubmit} noValidate>
      <Select
        label="Type"
        options={TRANSACTION_TYPE_OPTIONS}
        error={form.formState.errors.type?.message}
        {...form.register('type', {
          onChange: () => {
            form.setValue('categoryId', '');
            form.setValue('destinationAccountId', '');
          },
        })}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select
          label={isTransfer ? 'From account' : 'Account'}
          placeholder="Select account"
          options={accountOptions}
          error={form.formState.errors.accountId?.message}
          disabled={accounts.isLoading}
          {...form.register('accountId')}
        />
        {isTransfer ? (
          <Select
            label="To account"
            placeholder="Select account"
            options={destinationOptions}
            error={form.formState.errors.destinationAccountId?.message}
            disabled={accounts.isLoading}
            {...form.register('destinationAccountId')}
          />
        ) : (
          <Select
            label="Category"
            placeholder="Select category"
            options={categoryOptions}
            error={form.formState.errors.categoryId?.message}
            disabled={categories.isLoading}
            {...form.register('categoryId')}
          />
        )}
      </div>

      <TextField
        label="Amount"
        type="number"
        step="0.01"
        min="0"
        inputMode="decimal"
        placeholder="0.00"
        error={form.formState.errors.amount?.message}
        {...form.register('amount')}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <TextField
          label="Merchant"
          placeholder="e.g. Whole Foods"
          error={form.formState.errors.merchant?.message}
          {...form.register('merchant')}
        />
        <TextField
          label="Date"
          type="date"
          error={form.formState.errors.occurredAt?.message}
          {...form.register('occurredAt')}
        />
      </div>

      <TextField
        label="Note"
        placeholder="Optional details"
        error={form.formState.errors.note?.message}
        {...form.register('note')}
      />

      <div className="flex items-center justify-end gap-2">
        <Button type="button" variant="outline" onClick={onDone} disabled={pending}>
          Cancel
        </Button>
        <Button type="submit" disabled={pending}>
          {pending ? 'Saving…' : isEdit ? 'Save changes' : 'Add transaction'}
        </Button>
      </div>
    </form>
  );
}
