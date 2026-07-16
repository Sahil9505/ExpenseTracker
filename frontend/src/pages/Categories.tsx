import { useMemo, useState } from 'react';
import { Pencil, Plus, Tags, Trash2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { EmptyState } from '@/components/ui/empty-state';
import { Skeleton } from '@/components/ui/loading-state';
import { ConfirmDialog } from '@/components/ui/confirm-dialog';
import { useToast } from '@/components/ui/toast';
import { CategoryFormDialog } from '@/components/finance/CategoryFormDialog';
import { categoryIcon, colorOf } from '@/lib/finance';
import { useCategories, useDeleteCategory } from '@/hooks/useCategories';
import { ApiError } from '@/lib/api';
import type { Category, CategoryType } from '@/types';

export function Categories() {
  const { toast } = useToast();
  const query = useCategories();
  const deleteCategory = useDeleteCategory();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Category | null>(null);
  const [confirming, setConfirming] = useState<Category | null>(null);
  const [defaultType, setDefaultType] = useState<CategoryType>('EXPENSE');

  const grouped = useMemo(() => {
    const income: Category[] = [];
    const expense: Category[] = [];
    (query.data ?? []).forEach((category) => {
      if (category.type === 'INCOME') income.push(category);
      else expense.push(category);
    });
    return { income, expense };
  }, [query.data]);

  const openCreate = (type: CategoryType) => {
    setEditing(null);
    setDefaultType(type);
    setDialogOpen(true);
  };

  const openEdit = (category: Category) => {
    setEditing(category);
    setDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!confirming) return;
    try {
      await deleteCategory.mutateAsync(confirming.id);
      toast({ title: 'Category deleted', tone: 'success' });
    } catch (error) {
      toast({
        title: 'Could not delete category',
        description: error instanceof ApiError ? error.message : 'Please try again.',
        tone: 'danger',
      });
    } finally {
      setConfirming(null);
    }
  };

  const renderList = (categories: Category[], type: CategoryType) => {
    if (categories.length === 0) {
      return (
        <p className="px-1 py-2 text-sm text-muted-foreground">
          No {type.toLowerCase()} categories yet.
        </p>
      );
    }
    return (
      <ul className="flex flex-col gap-2">
        {categories.map((category) => {
          const Icon = categoryIcon(category.icon);
          const color = colorOf(category.color);
          return (
            <li
              key={category.id}
              className="flex items-center gap-3 rounded-lg border border-border bg-surface px-3 py-2.5"
            >
              <span
                className="flex h-9 w-9 items-center justify-center rounded-lg"
                style={{ backgroundColor: `${color}22`, color }}
              >
                <Icon className="h-4 w-4" aria-hidden="true" />
              </span>
              <span className="flex-1 truncate text-sm font-medium">{category.name}</span>
              {category.system ? <Badge variant="outline">System</Badge> : null}
              <Button variant="ghost" size="icon" onClick={() => openEdit(category)} aria-label={`Edit ${category.name}`}>
                <Pencil className="h-4 w-4" aria-hidden="true" />
              </Button>
              {category.system ? (
                <Button variant="ghost" size="icon" disabled aria-label="System categories cannot be deleted">
                  <span className="text-xs text-muted-foreground">🔒</span>
                </Button>
              ) : (
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-danger hover:bg-danger/10"
                  onClick={() => setConfirming(category)}
                  aria-label={`Delete ${category.name}`}
                >
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                </Button>
              )}
            </li>
          );
        })}
      </ul>
    );
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Categories</h2>
          <p className="text-sm text-muted-foreground">
            Sensible defaults are ready; add your own to match how you spend.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => openCreate('INCOME')}>
            <Plus className="h-4 w-4" aria-hidden="true" />
            Income
          </Button>
          <Button onClick={() => openCreate('EXPENSE')}>
            <Plus className="h-4 w-4" aria-hidden="true" />
            Expense
          </Button>
        </div>
      </div>

      {query.isLoading ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          <Skeleton className="h-64 w-full rounded-lg" />
          <Skeleton className="h-64 w-full rounded-lg" />
        </div>
      ) : query.isError ? (
        <div className="rounded-lg border border-border bg-surface p-6 text-sm">
          <p className="font-medium">We couldn't load your categories.</p>
          <Button variant="outline" size="sm" className="mt-3" onClick={() => query.refetch()}>
            Try again
          </Button>
        </div>
      ) : query.data && query.data.length === 0 ? (
        <EmptyState
          icon={Tags}
          title="No categories yet"
          description="Categories group your transactions so spending stays organized."
          action={
            <Button onClick={() => openCreate('EXPENSE')}>
              <Plus className="h-4 w-4" aria-hidden="true" />
              Add category
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Income</CardTitle>
            </CardHeader>
            <CardContent>{renderList(grouped.income, 'INCOME')}</CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Expense</CardTitle>
            </CardHeader>
            <CardContent>{renderList(grouped.expense, 'EXPENSE')}</CardContent>
          </Card>
        </div>
      )}

      <CategoryFormDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        category={editing}
        defaultType={defaultType}
      />

      <ConfirmDialog
        open={Boolean(confirming)}
        title="Delete this category?"
        description={
          confirming
            ? `This removes "${confirming.name}" for good. Categories attached to transactions can't be deleted.`
            : undefined
        }
        confirmLabel="Delete"
        loading={deleteCategory.isPending}
        onConfirm={confirmDelete}
        onClose={() => setConfirming(null)}
      />
    </div>
  );
}
