import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';

interface ProfileData {
  id: number;
  email: string;
  name: string;
  role: string;
  active: boolean;
  createdAt?: string;
}

export default function ProfilePage() {
  const { isAuthenticated } = useAuth();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated) return;
    client.get('/auth/me')
      .then(res => setProfile(res.data.data))
      .catch(() => setError('Failed to load profile'))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Please <Link to="/login" className="text-blue-600">sign in</Link> to view your profile.</p>
      </div>
    );
  }

  const roleLabel: Record<string, string> = {
    ROLE_USER: 'User',
    ROLE_ORGANIZER: 'Organizer',
    ROLE_ADMIN: 'Admin',
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">Profile</h1>
          <Link to="/" className="text-blue-600 hover:underline">Home</Link>
        </div>
      </header>

      <main className="max-w-xl mx-auto px-4 py-6">
        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : error ? (
          <p className="text-red-500 text-center">{error}</p>
        ) : profile ? (
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center">
                <span className="text-2xl font-bold text-blue-600">
                  {profile.name.charAt(0).toUpperCase()}
                </span>
              </div>
              <div>
                <h2 className="text-xl font-semibold">{profile.name}</h2>
                <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${
                  profile.role === 'ROLE_ADMIN' ? 'bg-red-100 text-red-700' :
                  profile.role === 'ROLE_ORGANIZER' ? 'bg-purple-100 text-purple-700' :
                  'bg-blue-100 text-blue-700'
                }`}>
                  {roleLabel[profile.role] || profile.role}
                </span>
              </div>
            </div>

            <dl className="space-y-4">
              <div className="flex justify-between border-b border-gray-100 pb-3">
                <dt className="text-sm text-gray-500">Email</dt>
                <dd className="text-sm font-medium">{profile.email}</dd>
              </div>
              <div className="flex justify-between border-b border-gray-100 pb-3">
                <dt className="text-sm text-gray-500">Account Status</dt>
                <dd className={`text-sm font-medium ${profile.active ? 'text-green-600' : 'text-red-600'}`}>
                  {profile.active ? 'Active' : 'Inactive'}
                </dd>
              </div>
              {profile.createdAt && (
                <div className="flex justify-between">
                  <dt className="text-sm text-gray-500">Member Since</dt>
                  <dd className="text-sm font-medium">{new Date(profile.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</dd>
                </div>
              )}
            </dl>
          </div>
        ) : null}
      </main>
    </div>
  );
}
