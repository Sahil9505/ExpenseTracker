/**
 * Token storage strategy. Tokens live in localStorage so they survive reloads and
 * back/forward navigation. The API client reads them to attach the Bearer header
 * and to drive silent refresh; the auth provider mirrors them into React state.
 */
const ACCESS_TOKEN_KEY = 'nova.accessToken';
const REFRESH_TOKEN_KEY = 'nova.refreshToken';

export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },
  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },
  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  },
  hasTokens(): boolean {
    return Boolean(localStorage.getItem(ACCESS_TOKEN_KEY));
  },
};
