import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import type { UserFollowing, CourtFollowing } from '../api/types';
import { useAuth } from '../context/AuthContext';

export default function FollowingPage() {
  const { isAuthenticated } = useAuth();
  const [tab, setTab] = useState<'orgs' | 'courts'>('orgs');
  const [orgFollowing, setOrgFollowing] = useState<UserFollowing[]>([]);
  const [courtFollowing, setCourtFollowing] = useState<CourtFollowing[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) return;
    Promise.all([
      client.get('/user/following'),
      client.get('/user/following/courts'),
    ]).then(([orgRes, courtRes]) => {
      setOrgFollowing(orgRes.data.data || []);
      setCourtFollowing(courtRes.data.data || []);
    }).finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Please <Link to="/login" className="text-blue-600">sign in</Link> to view your followed organizations and courts.</p>
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
            onClick={() => setTab('courts')}
            className={`flex-1 py-2 rounded-md text-sm font-medium transition ${
              tab === 'courts' ? 'bg-white shadow text-gray-900' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Courts ({courtFollowing.length})
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
          courtFollowing.length === 0 ? (
            <p className="text-gray-400 text-center">You are not following any courts yet.</p>
          ) : (
            <div className="space-y-3">
              {courtFollowing.map(f => (
                <Link
                  key={f.id}
                  to={`/venues/${f.court.venueId}`}
                  className="block bg-white rounded-lg shadow-sm p-4 border border-gray-200 hover:border-blue-300 transition"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center text-green-600 font-bold">
                      {f.court.courtNumber}
                    </div>
                    <div>
                      <h3 className="font-medium text-blue-600">Court {f.court.courtNumber}</h3>
                      <p className="text-sm text-gray-500">{f.court.venueName || 'Unknown venue'} · ${f.court.pricePerHourSgd}/hr</p>
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
