import type { FieldValues, Path, UseFormSetError } from 'react-hook-form';
import { getFieldErrors } from './api';
import type { ApiFieldError } from '@/types';

/**
 * Copies server-side field errors into a React Hook Form instance for the given
 * set of known fields. Unknown fields are ignored so they don't break the form.
 */
export function applyFieldErrors<T extends FieldValues>(
  setError: UseFormSetError<T>,
  error: unknown,
  known: Path<T>[],
): void {
  const fields: ApiFieldError[] = getFieldErrors(error);
  const knownFields = known as string[];
  fields.forEach((field) => {
    if (knownFields.includes(field.field)) {
      setError(field.field as Path<T>, { message: field.message });
    }
  });
}
