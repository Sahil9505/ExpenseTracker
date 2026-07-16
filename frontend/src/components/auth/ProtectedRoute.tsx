import { Navigate, useLocation } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { useAuth } from '@/context/AuthProvider';
import { ScreenLoader } from './ScreenLoader';

/**
 * Gate for authenticated areas. While the session is being restored it shows a
 * loader; unauthenticated visitors are redirected to /login with their intended
 * destination preserved so they return after signing in.
 */
export function ProtectedRoute() {
  const { status } = useAuth();
  const location = useLocation();

  if (status === 'loading') {
    return <ScreenLoader />;
  }

  if (status === 'unauthenticated') {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <AppLayout />;
}
