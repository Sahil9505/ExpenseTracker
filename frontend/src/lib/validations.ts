import { z } from 'zod';

export const COMMON_CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CAD', 'AUD', 'INR', 'SGD', 'CHF'] as const;

export const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

export type LoginValues = z.infer<typeof loginSchema>;

export const registerSchema = z
  .object({
    fullName: z.string().max(255, 'Name is too long').optional().or(z.literal('')),
    email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
    password: z
      .string()
      .min(8, 'Use at least 8 characters')
      .max(128, 'Password is too long'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    preferredCurrency: z.enum(COMMON_CURRENCIES).optional(),
  })
  .refine((values) => values.password === values.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

export type RegisterValues = z.infer<typeof registerSchema>;

export const forgotPasswordSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
});

export type ForgotPasswordValues = z.infer<typeof forgotPasswordSchema>;

export const profileSchema = z.object({
  fullName: z.string().max(255, 'Name is too long').optional().or(z.literal('')),
  preferredCurrency: z.enum(COMMON_CURRENCIES).optional().or(z.literal('')),
  timezone: z.string().max(64, 'Timezone is too long').optional().or(z.literal('')),
  avatarUrl: z
    .string()
    .max(512, 'Avatar URL is too long')
    .url('Enter a valid URL')
    .optional()
    .or(z.literal('')),
});

export type ProfileValues = z.infer<typeof profileSchema>;

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z
      .string()
      .min(8, 'Use at least 8 characters')
      .max(128, 'Password is too long'),
    confirmNewPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine((values) => values.newPassword === values.confirmNewPassword, {
    message: 'New passwords do not match',
    path: ['confirmNewPassword'],
  });

export type ChangePasswordValues = z.infer<typeof changePasswordSchema>;
