import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import HomePage from './pages/HomePage';
import VenueDetailPage from './pages/VenueDetailPage';
import MyBookingsPage from './pages/MyBookingsPage';
import ProfilePage from './pages/ProfilePage';
import OrgDetailPage from './pages/OrgDetailPage';
import ActivityDetailPage from './pages/ActivityDetailPage';
import FollowingPage from './pages/FollowingPage';
import SubmitVenuePage from './pages/organizer/SubmitVenuePage';
import MyVenuesPage from './pages/organizer/MyVenuesPage';
import EditVenuePage from './pages/organizer/EditVenuePage';
import AddCourtPage from './pages/organizer/AddCourtPage';
import OrgListPage from './pages/organizer/OrgListPage';
import OrgManagePage from './pages/organizer/OrgDetailPage';
import CreateOrgPage from './pages/organizer/CreateOrgPage';
import CreateActivityPage from './pages/organizer/CreateActivityPage';
import ProtectedRoute from './components/ProtectedRoute';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminApprovalsPage from './pages/admin/AdminApprovalsPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminVenueApprovalsPage from './pages/admin/AdminVenueApprovalsPage';
import AdminOrgApprovalsPage from './pages/admin/AdminOrgApprovalsPage';
import AdminCreateVenuePage from './pages/admin/AdminCreateVenuePage';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<HomePage />} />
            <Route path="/venues/:id" element={<VenueDetailPage />} />
            <Route path="/orgs/:id" element={<OrgDetailPage />} />
            <Route path="/activities/:id" element={<ActivityDetailPage />} />

            {/* Authenticated user */}
            <Route path="/bookings" element={<MyBookingsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/following" element={<ProtectedRoute><FollowingPage /></ProtectedRoute>} />

            {/* Organizer */}
            <Route path="/organizer/venues" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><MyVenuesPage /></ProtectedRoute>} />
            <Route path="/organizer/venues/new" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><SubmitVenuePage /></ProtectedRoute>} />
            <Route path="/organizer/venues/:id/edit" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><EditVenuePage /></ProtectedRoute>} />
            <Route path="/organizer/venues/:venueId/courts/new" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><AddCourtPage /></ProtectedRoute>} />
            <Route path="/organizer/orgs" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><OrgListPage /></ProtectedRoute>} />
            <Route path="/organizer/orgs/new" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><CreateOrgPage /></ProtectedRoute>} />
            <Route path="/organizer/orgs/:id" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><OrgManagePage /></ProtectedRoute>} />
            <Route path="/organizer/orgs/:orgId/activities/new" element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><CreateActivityPage /></ProtectedRoute>} />

            {/* Admin */}
            <Route path="/admin" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminDashboardPage /></ProtectedRoute>} />
            <Route path="/admin/approvals" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminApprovalsPage /></ProtectedRoute>} />
            <Route path="/admin/venue-approvals" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminVenueApprovalsPage /></ProtectedRoute>} />
            <Route path="/admin/org-approvals" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminOrgApprovalsPage /></ProtectedRoute>} />
            <Route path="/admin/users" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminUsersPage /></ProtectedRoute>} />
            <Route path="/admin/venues/new" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminCreateVenuePage /></ProtectedRoute>} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
