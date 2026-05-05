import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import type { UserFollowing } from '../api/types';
import { useAuth } from '../context/AuthContext';

export default function FollowingPage() {
  const { isAuthenticated } = useAuth();
  const [following, setFollowing] = useState<UserFollowing[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) return;
    client.get('/user/following')
      .then(res => setFollowing(res.data.data))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Please <Link to="/login" className="text-blue-600">sign in</Link> to view your followed organizations.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">Following</h1>
          <Link to="/" className="text-blue-600 hover:underline">Home</Link>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : following.length === 0 ? (
          <p className="text-gray-400 text-center">You are not following any organizations yet.</p>
        ) : (
          <div className="space-y-3">
            {following.map(f => (
              <Link
                key={f.id}
                to={`/orgs/${f.org.id}`}
                className="block bg-white rounded-lg shadow-sm p-4 border border-gray-200 hover:border-blue-300 transition"
              >
                <div className="flex items-center gap-3">
                  {f.org.logoUrl ? (
                    <img src={f.org.logoUrl} alt={f.org.name} className="w-10 h-10 rounded-lg object-cover" />
                  ) : (
                    <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
                      {f.org.name.charAt(0)}
                    </div>
                  )}
                  <div>
                    <h3 className="font-medium text-blue-600">{f.org.name}</h3>
                    <p className="text-sm text-gray-500">{f.org.description || 'No description'}</p>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
