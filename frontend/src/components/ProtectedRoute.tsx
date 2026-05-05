import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: string;
}

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="bg-white rounded-lg shadow-sm p-8 text-center max-w-md">
          <h2 className="text-xl font-semibold text-gray-800 mb-2">Access Denied</h2>
          <p className="text-gray-500 mb-1">
            You do not have permission to view this page.
          </p>
          <p className="text-sm text-gray-400">
            Required role: <span className="font-medium">{requiredRole.replace('ROLE_', '')}</span>
            {user && (
              <>
                <br />
                Your role: <span className="font-medium">{user.role.replace('ROLE_', '')}</span>
              </>
            )}
          </p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
