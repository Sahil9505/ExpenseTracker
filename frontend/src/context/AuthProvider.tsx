import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { api } from '@/lib/api';
import { tokenStorage } from '@/lib/auth';
import type { AuthTokens, CurrentUser } from '@/types';

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

interface AuthContextValue {
  user: CurrentUser | null;
  status: AuthStatus;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<CurrentUser>;
  register: (input: {
    email: string;
    password: string;
    fullName?: string;
    preferredCurrency?: string;
  }) => Promise<CurrentUser>;
  logout: () => Promise<void>;
  setUser: (user: CurrentUser | null) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function applyTokens(tokens: AuthTokens): CurrentUser {
  tokenStorage.setTokens(tokens.accessToken, tokens.refreshToken);
  return tokens.user;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [status, setStatus] = useState<AuthStatus>('loading');

  // On mount, recover the session if tokens exist.
  useEffect(() => {
    let active = true;
    if (!tokenStorage.hasTokens()) {
      setStatus('unauthenticated');
      return;
    }
    api
      .get<CurrentUser>('/api/users/me')
      .then((current) => {
        if (!active) return;
        setUser(current);
        setStatus('authenticated');
      })
      .catch(() => {
        if (!active) return;
        tokenStorage.clear();
        setStatus('unauthenticated');
      });
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const tokens = await api.post<AuthTokens>('/api/auth/login', { email, password });
    const current = applyTokens(tokens);
    setUser(current);
    setStatus('authenticated');
    return current;
  }, []);

  const register = useCallback(
    async (input: {
      email: string;
      password: string;
      fullName?: string;
      preferredCurrency?: string;
    }) => {
      const tokens = await api.post<AuthTokens>('/api/auth/register', input);
      const current = applyTokens(tokens);
      setUser(current);
      setStatus('authenticated');
      return current;
    },
    [],
  );

  const logout = useCallback(async () => {
    const refreshToken = tokenStorage.getRefreshToken();
    try {
      await api.post<void>('/api/auth/logout', refreshToken ? { refreshToken } : {});
    } catch {
      /* best-effort: clear locally regardless of server response */
    }
    tokenStorage.clear();
    setUser(null);
    setStatus('unauthenticated');
  }, []);

  const setUserCallback = useCallback((next: CurrentUser | null) => {
    setUser(next);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      status,
      isAuthenticated: status === 'authenticated',
      login,
      register,
      logout,
      setUser: setUserCallback,
    }),
    [user, status, login, register, logout, setUserCallback],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
