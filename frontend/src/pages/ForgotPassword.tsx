import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { AuthLayout } from '@/components/auth/AuthLayout';
import { TextField } from '@/components/auth/TextField';
import { Button } from '@/components/ui/button';
import { useToast } from '@/components/ui/toast';
import { forgotPasswordSchema, type ForgotPasswordValues } from '@/lib/validations';

export function ForgotPassword() {
  const { toast } = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: '' },
  });

  // Shell only: backend password reset is not implemented in this release.
  // Submitting surfaces a clear, non-functional notice instead of calling an API.
  const onSubmit = () => {
    toast({
      title: 'Password reset is not available yet',
      description: 'This feature arrives in a future Nova release.',
      tone: 'warning',
    });
  };

  return (
    <AuthLayout title="Reset your password" subtitle="Enter your email and we'll send reset instructions.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
        />
        <Button type="submit" size="lg" className="w-full">
          Send reset link
        </Button>
      </form>

      <div className="mt-4 rounded-md border border-border bg-surface-2 px-3 py-2 text-xs text-muted-foreground">
        Password reset is coming soon. This form is a preview and does not send email yet.
      </div>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        <Link to="/login" className="font-medium text-primary hover:underline">
          Back to sign in
        </Link>
      </p>
    </AuthLayout>
  );
}
