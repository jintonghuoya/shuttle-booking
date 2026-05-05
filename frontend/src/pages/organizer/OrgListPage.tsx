import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client';
import type { Organization } from '../../api/types';

export default function OrgListPage() {
  const [orgs, setOrgs] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/orgs/mine')
      .then(res => setOrgs(res.data.data))
      .catch(() => alert('Failed to load organizations'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">My Organizations</h1>
          <div className="flex gap-4">
            <Link to="/" className="text-blue-600 hover:underline">Home</Link>
            <Link to="/organizer/orgs/new" className="text-blue-600 hover:underline">Create New</Link>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : orgs.length === 0 ? (
          <div className="text-center">
            <p className="text-gray-500">You have not created any organizations yet.</p>
            <Link
              to="/organizer/orgs/new"
              className="inline-block mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
            >
              Create Organization
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {orgs.map(org => (
              <Link
                key={org.id}
                to={`/organizer/orgs/${org.id}`}
                className="block bg-white rounded-lg shadow-sm p-4 border border-gray-200 hover:border-blue-300 transition"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-lg font-semibold">{org.name}</h2>
                    {org.description && <p className="text-sm text-gray-500 mt-1">{org.description}</p>}
                  </div>
                  <span className={org.active ? 'text-green-600 text-sm font-medium' : 'text-red-600 text-sm font-medium'}>
                    {org.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
