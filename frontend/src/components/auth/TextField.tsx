import { forwardRef, useState, type InputHTMLAttributes } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { cn } from '@/lib/utils';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
}

/**
 * Labeled text input with accessible error text. Renders a show/hide toggle when
 * used as a password field. Forwards the ref so React Hook Form can register it.
 */
export const TextField = forwardRef<HTMLInputElement, TextFieldProps>(
  ({ label, error, hint, id, className, type = 'text', ...props }, ref) => {
    const [visible, setVisible] = useState(false);
    const isPassword = type === 'password';
    const inputType = isPassword ? (visible ? 'text' : 'password') : type;
    const fieldId = id ?? props.name;

    return (
      <div className="flex flex-col gap-1.5">
        <label htmlFor={fieldId} className="text-sm font-medium text-foreground">
          {label}
        </label>
        <div className="relative">
          <Input
            id={fieldId}
            ref={ref}
            type={inputType}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? `${fieldId}-error` : undefined}
            className={cn(
              'w-full',
              isPassword && 'pr-10',
              error && 'border-danger focus-visible:ring-danger/70',
              className,
            )}
            {...props}
          />
          {isPassword && (
            <button
              type="button"
              onClick={() => setVisible((value) => !value)}
              tabIndex={-1}
              aria-label={visible ? 'Hide password' : 'Show password'}
              className="absolute inset-y-0 right-0 flex items-center pr-3 text-muted-foreground transition-colors hover:text-foreground"
            >
              {visible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          )}
        </div>
        {error ? (
          <p id={`${fieldId}-error`} className="text-xs font-medium text-danger">
            {error}
          </p>
        ) : hint ? (
          <p className="text-xs text-muted-foreground">{hint}</p>
        ) : null}
      </div>
    );
  },
);
TextField.displayName = 'TextField';
