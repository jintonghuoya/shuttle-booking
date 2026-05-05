import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import type { UserFollowing, VenueFollowing } from '../api/types';
import { useAuth } from '../context/AuthContext';

export default function FollowingPage() {
  const { isAuthenticated } = useAuth();
  const [tab, setTab] = useState<'orgs' | 'venues'>('orgs');
  const [orgFollowing, setOrgFollowing] = useState<UserFollowing[]>([]);
  const [venueFollowing, setVenueFollowing] = useState<VenueFollowing[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) return;
    Promise.all([
      client.get('/user/following'),
      client.get('/user/following/venues'),
    ]).then(([orgRes, venueRes]) => {
      setOrgFollowing(orgRes.data.data || []);
      setVenueFollowing(venueRes.data.data || []);
    }).finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Please <Link to="/login" className="text-blue-600">sign in</Link> to view your followed organizations and venues.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">My Following</h1>
          <Link to="/" className="text-blue-600 hover:underline">Home</Link>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {/* Tabs */}
        <div className="flex gap-1 bg-gray-200 rounded-lg p-1 mb-6">
          <button
            onClick={() => setTab('orgs')}
            className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
              tab === 'orgs' ? 'bg-white shadow text-gray-900' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Organizations ({orgFollowing.length})
          </button>
          <button
            onClick={() => setTab('venues')}
            className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
              tab === 'venues' ? 'bg-white shadow text-gray-900' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Venues ({venueFollowing.length})
          </button>
        </div>

        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : tab === 'orgs' ? (
          orgFollowing.length === 0 ? (
            <p className="text-gray-400 text-center">You are not following any organizations yet.</p>
          ) : (
            <div className="space-y-3">
              {orgFollowing.map(f => (
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
                    <span className="ml-auto text-xl">❤️</span>
                  </div>
                </Link>
              ))}
            </div>
          )
        ) : (
          venueFollowing.length === 0 ? (
            <p className="text-gray-400 text-center">You are not following any venues yet.</p>
          ) : (
            <div className="space-y-3">
              {venueFollowing.map(f => (
                <Link
                  key={f.id}
                  to={`/venues/${f.venue.id}`}
                  className="block bg-white rounded-lg shadow-sm p-4 border border-gray-200 hover:border-blue-300 transition"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center text-green-600 font-bold text-sm">
                      {f.venue.name.charAt(0)}
                    </div>
                    <div>
                      <h3 className="font-medium text-blue-600">{f.venue.name}</h3>
                      <p className="text-sm text-gray-500">{f.venue.address}</p>
                    </div>
                    <span className="ml-auto text-xl">❤️</span>
                  </div>
                </Link>
              ))}
            </div>
          )
        )}
      </main>
    </div>
  );
}
