import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '@/components/auth/AuthLayout';
import { TextField } from '@/components/auth/TextField';
import { Button } from '@/components/ui/button';
import { useToast } from '@/components/ui/toast';
import { useAuth } from '@/context/AuthProvider';
import { ApiError, getFieldErrors } from '@/lib/api';
import { applyFieldErrors } from '@/lib/formErrors';
import { COMMON_CURRENCIES, registerSchema, type RegisterValues } from '@/lib/validations';

const selectClass =
  'flex h-10 w-full rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/70 focus-visible:ring-offset-2 ' +
  'focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50';

export function Register() {
  const { register } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  const {
    register: registerField,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { fullName: '', email: '', password: '', confirmPassword: '', preferredCurrency: undefined },
  });

  const onSubmit = async (values: RegisterValues) => {
    try {
      const user = await register({
        email: values.email,
        password: values.password,
        fullName: values.fullName?.trim() || undefined,
        preferredCurrency: values.preferredCurrency || undefined,
      });
      const firstName = user.fullName?.split(' ')[0];
      toast({
        title: firstName ? `Welcome, ${firstName}` : 'Welcome to Nova',
        description: 'Your account is ready.',
        tone: 'success',
      });
      navigate('/', { replace: true });
    } catch (error) {
      applyFieldErrors(setError, error, ['email', 'password', 'fullName', 'preferredCurrency']);
      const fields = getFieldErrors(error);
      if (fields.length === 0) {
        toast({
          title: 'Could not create account',
          description: error instanceof ApiError ? error.message : 'Please try again.',
          tone: 'danger',
        });
      }
    }
  };

  return (
    <AuthLayout title="Create your account" subtitle="Start managing your money with clarity.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <TextField
          label="Full name"
          autoComplete="name"
          placeholder="Alex Morgan"
          error={errors.fullName?.message}
          {...registerField('fullName')}
        />
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...registerField('email')}
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="new-password"
          placeholder="At least 8 characters"
          error={errors.password?.message}
          {...registerField('password')}
        />
        <TextField
          label="Confirm password"
          type="password"
          autoComplete="new-password"
          placeholder="Re-enter your password"
          error={errors.confirmPassword?.message}
          {...registerField('confirmPassword')}
        />
        <div className="flex flex-col gap-1.5">
          <label htmlFor="preferredCurrency" className="text-sm font-medium text-foreground">
            Preferred currency
          </label>
          <select
            id="preferredCurrency"
            className={selectClass}
            defaultValue="USD"
            {...registerField('preferredCurrency')}
          >
            {COMMON_CURRENCIES.map((currency) => (
              <option key={currency} value={currency}>
                {currency}
              </option>
            ))}
          </select>
        </div>
        <Button type="submit" size="lg" disabled={isSubmitting} className="mt-2 w-full">
          {isSubmitting ? 'Creating account…' : 'Create account'}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-primary hover:underline">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  );
}
