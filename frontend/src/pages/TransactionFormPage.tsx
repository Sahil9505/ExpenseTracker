import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { LoadingState } from '@/components/ui/loading-state';
import { useTransaction } from '@/hooks/useTransactions';
import { TransactionForm } from '@/components/finance/TransactionForm';

/**
 * Page wrapper for creating a transaction (/transactions/new) or editing one
 * (/transactions/:id/edit). Loads the existing record for edits and hands it to
 * the shared TransactionForm.
 */
export function TransactionFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const query = useTransaction(id);

  const isEdit = Boolean(id);
  const title = isEdit ? 'Edit transaction' : 'New transaction';

  const goBack = () => navigate('/transactions');

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-3">
        <Button
          variant="outline"
          size="icon"
          aria-label="Back to transactions"
          onClick={goBack}
        >
          <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        </Button>
        <div>
          <h2 className="text-2xl font-bold tracking-tight">{title}</h2>
          <p className="text-sm text-muted-foreground">
            {isEdit
              ? 'Update the details of this transaction.'
              : 'Record income, an expense, or a transfer between accounts.'}
          </p>
        </div>
      </div>

      <Card>
        <CardContent>
          {isEdit ? (
            query.isLoading ? (
              <LoadingState label="Loading transaction…" />
            ) : query.isError ? (
              <div className="flex flex-col items-start gap-3 rounded-lg border border-border bg-surface p-6">
                <p className="text-sm font-medium">We couldn't load this transaction.</p>
                <Button variant="outline" size="sm" onClick={() => query.refetch()}>
                  Try again
                </Button>
              </div>
            ) : query.data ? (
              <TransactionForm transaction={query.data} onDone={goBack} />
            ) : null
          ) : (
            <TransactionForm onDone={goBack} />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
