import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import client from '../../api/client';
import type { Approval } from '../../api/types';

export default function AdminDashboardPage() {
  const { user } = useAuth();
  const [pendingCount, setPendingCount] = useState(0);
  const [userCount, setUserCount] = useState(0);
  const [venueCount, setVenueCount] = useState(0);
  const [recentApprovals, setRecentApprovals] = useState<Approval[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      client.get('/admin/approvals'),
      client.get('/admin/users'),
      client.get('/venues'),
    ])
      .then(([approvalsRes, usersRes, venuesRes]) => {
        const approvals = approvalsRes.data.data;
        setPendingCount(approvals.length);
        setRecentApprovals(approvals.slice(0, 5));
        setUserCount(usersRes.data.data.length);
        setVenueCount(venuesRes.data.data.totalElements);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">Admin Dashboard</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-500">{user?.name}</span>
            <Link to="/" className="text-blue-600 hover:underline text-sm">Home</Link>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-6 space-y-6">
        {loading ? (
          <p className="text-gray-400 text-center py-8">Loading...</p>
        ) : (
          <>
            {/* Stats Overview */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="bg-white rounded-lg shadow-sm p-6">
                <p className="text-sm font-medium text-gray-500">Pending Approvals</p>
                <p className="mt-1 text-3xl font-bold text-yellow-600">{pendingCount}</p>
              </div>
              <div className="bg-white rounded-lg shadow-sm p-6">
                <p className="text-sm font-medium text-gray-500">Total Users</p>
                <p className="mt-1 text-3xl font-bold text-blue-600">{userCount}</p>
              </div>
              <div className="bg-white rounded-lg shadow-sm p-6">
                <p className="text-sm font-medium text-gray-500">Total Venues</p>
                <p className="mt-1 text-3xl font-bold text-green-600">{venueCount}</p>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Link
                to="/admin/approvals"
                className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition block"
              >
                <h2 className="text-lg font-semibold mb-1">Venue Approvals</h2>
                <p className="text-gray-500 text-sm">Review and approve venue submissions</p>
                {pendingCount > 0 && (
                  <span className="inline-block mt-2 px-2 py-0.5 bg-yellow-100 text-yellow-700 text-xs font-medium rounded">
                    {pendingCount} pending
                  </span>
                )}
              </Link>
              <Link
                to="/admin/venue-approvals"
                className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition block"
              >
                <h2 className="text-lg font-semibold mb-1">All Venue Approvals</h2>
                <p className="text-gray-500 text-sm">View all approval requests with status filtering</p>
              </Link>
              <Link
                to="/admin/users"
                className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition block"
              >
                <h2 className="text-lg font-semibold mb-1">User Management</h2>
                <p className="text-gray-500 text-sm">Manage user accounts and roles</p>
              </Link>
            </div>

            {/* Recent Activity */}
            <div className="bg-white rounded-lg shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold">Recent Approval Requests</h2>
              </div>
              {recentApprovals.length === 0 ? (
                <div className="px-6 py-8 text-center text-gray-400 text-sm">
                  No recent approval requests
                </div>
              ) : (
                <div className="divide-y divide-gray-100">
                  {recentApprovals.map(a => (
                    <div key={a.id} className="px-6 py-3 flex items-center justify-between">
                      <div className="min-w-0">
                        <p className="font-medium text-sm truncate">{a.venueName}</p>
                        <p className="text-xs text-gray-500">
                          Submitted by {a.submittedByName} &middot; {new Date(a.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                      <span className={`ml-4 shrink-0 text-xs font-medium px-2 py-1 rounded ${
                        a.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                        a.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                        'bg-red-100 text-red-700'
                      }`}>
                        {a.status}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
