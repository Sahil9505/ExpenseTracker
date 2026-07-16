import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { AuthLayout } from '@/components/auth/AuthLayout';
import { TextField } from '@/components/auth/TextField';
import { Button } from '@/components/ui/button';
import { useToast } from '@/components/ui/toast';
import { useAuth } from '@/context/AuthProvider';
import { ApiError, getFieldErrors } from '@/lib/api';
import { applyFieldErrors } from '@/lib/formErrors';
import { loginSchema, type LoginValues } from '@/lib/validations';

interface LocationState {
  from?: { pathname?: string };
}

export function Login() {
  const { login } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as LocationState | null)?.from?.pathname ?? '/';

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = async (values: LoginValues) => {
    try {
      const user = await login(values.email, values.password);
      const firstName = user.fullName?.split(' ')[0];
      toast({
        title: firstName ? `Welcome back, ${firstName}` : 'Welcome back',
        description: 'You are signed in.',
        tone: 'success',
      });
      navigate(from, { replace: true });
    } catch (error) {
      applyFieldErrors(setError, error, ['email', 'password']);
      const fields = getFieldErrors(error);
      if (fields.length === 0) {
        toast({
          title: 'Unable to sign in',
          description: error instanceof ApiError ? error.message : 'Please check your details and try again.',
          tone: 'danger',
        });
      }
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to your Nova account to continue.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="current-password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register('password')}
        />
        <div className="flex justify-end">
          <Link
            to="/forgot-password"
            className="text-sm font-medium text-primary transition-colors hover:underline"
          >
            Forgot password?
          </Link>
        </div>
        <Button type="submit" size="lg" disabled={isSubmitting} className="w-full">
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        New to Nova?{' '}
        <Link to="/register" className="font-medium text-primary hover:underline">
          Create an account
        </Link>
      </p>
    </AuthLayout>
  );
}
