/** Health payload returned by GET /api/health. */
export interface HealthStatus {
  status: 'UP' | 'DOWN';
  service: string;
  timestamp: string;
}

export type TrendDirection = 'up' | 'down' | 'flat';

export interface StatMetric {
  id: string;
  label: string;
  value: number;
  format: 'currency' | 'percent' | 'number';
  currency?: string;
  trend: number;
  trendDirection: TrendDirection;
  caption: string;
}

export interface SpendingPoint {
  month: string;
  income: number;
  expenses: number;
}

export interface CategoryBreakdown {
  category: string;
  amount: number;
  color: string;
}

export interface TransactionItem {
  id: string;
  description: string;
  category: string;
  amount: number;
  date: string;
  type: 'income' | 'expense';
}

export type UserRole = 'USER' | 'ADMIN';
export type AccountStatus = 'ACTIVE' | 'DISABLED' | 'LOCKED' | 'PENDING';

/** The authenticated user as returned by the API. Never includes secrets. */
export interface CurrentUser {
  id: string;
  email: string;
  fullName: string | null;
  role: UserRole;
  accountStatus: AccountStatus;
  preferredCurrency: string;
  timezone: string | null;
  avatarUrl: string | null;
  lastLoginAt: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Tokens issued on register, login, and refresh. */
export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: CurrentUser;
}

/** Field-level validation error returned by the API. */
export interface ApiFieldError {
  field: string;
  message: string;
}
