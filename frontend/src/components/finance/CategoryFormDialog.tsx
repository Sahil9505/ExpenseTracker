import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiError, getFieldErrors } from '@/lib/api';
import { applyFieldErrors } from '@/lib/formErrors';
import { useToast } from '@/components/ui/toast';
import { Dialog } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { TextField } from '@/components/auth/TextField';
import { categorySchema, type CategoryValues } from '@/lib/validations';
import { ICON_CHOICES } from '@/lib/finance';
import { useCreateCategory, useUpdateCategory } from '@/hooks/useCategories';
import type { Category, CategoryType } from '@/types';
import { cn } from '@/lib/utils';

interface CategoryFormDialogProps {
  open: boolean;
  onClose: () => void;
  category?: Category | null;
  defaultType?: CategoryType;
}

export function CategoryFormDialog({ open, onClose, category, defaultType }: CategoryFormDialogProps) {
  const { toast } = useToast();
  const isEdit = Boolean(category);
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();

  const form = useForm<CategoryValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      name: '',
      type: defaultType ?? 'EXPENSE',
      color: '#3B82F6',
      icon: '',
    },
  });

  // Re-seed the form every time the dialog opens so a fresh create (with the
  // chosen default type) or an edit always starts from the right values.
  useEffect(() => {
    if (!open) return;
    form.reset({
      name: category?.name ?? '',
      type: category?.type ?? defaultType ?? 'EXPENSE',
      color: category?.color ?? '#3B82F6',
      icon: category?.icon ?? '',
    });
  }, [open, category, defaultType, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    const payload = {
      name: values.name.trim(),
      type: values.type,
      color: values.color || undefined,
      icon: values.icon?.trim() || undefined,
    };
    try {
      if (isEdit && category) {
        await updateCategory.mutateAsync({ id: category.id, payload });
        toast({ title: 'Category updated', tone: 'success' });
      } else {
        await createCategory.mutateAsync(payload);
        toast({ title: 'Category created', tone: 'success' });
      }
      onClose();
    } catch (error) {
      applyFieldErrors(form.setError, error, ['name', 'type', 'color', 'icon']);
      if (getFieldErrors(error).length === 0) {
        toast({
          title: isEdit ? 'Could not update category' : 'Could not create category',
          description: error instanceof ApiError ? error.message : 'Please try again.',
          tone: 'danger',
        });
      }
    }
  });

  const pending = createCategory.isPending || updateCategory.isPending;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={isEdit ? 'Edit category' : 'Add category'}
      description="Group your income and expenses so reports stay meaningful."
      footer={
        <div className="flex justify-end gap-2">
          <Button variant="outline" type="button" onClick={onClose} disabled={pending}>
            Cancel
          </Button>
          <Button type="button" onClick={onSubmit} disabled={pending}>
            {pending ? 'Saving…' : isEdit ? 'Save changes' : 'Create category'}
          </Button>
        </div>
      }
    >
      <form className="flex flex-col gap-4" onSubmit={onSubmit} noValidate>
        <TextField
          label="Name"
          placeholder="Groceries"
          error={form.formState.errors.name?.message}
          {...form.register('name')}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select
            label="Type"
            options={[
              { value: 'EXPENSE', label: 'Expense' },
              { value: 'INCOME', label: 'Income' },
            ]}
            error={form.formState.errors.type?.message}
            {...form.register('type')}
          />
          <Select
            label="Icon"
            placeholder="Choose an icon"
            options={ICON_CHOICES}
            error={form.formState.errors.icon?.message}
            {...form.register('icon')}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="category-color" className="text-sm font-medium text-foreground">
            Color
          </label>
          <div className="flex items-center gap-3">
            <input
              id="category-color"
              type="color"
              className={cn(
                'h-10 w-14 cursor-pointer rounded-md border border-border bg-surface',
                'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/70',
              )}
              {...form.register('color')}
            />
            <span className="text-sm text-muted-foreground">Shown on transactions and charts</span>
          </div>
        </div>
      </form>
    </Dialog>
  );
}
