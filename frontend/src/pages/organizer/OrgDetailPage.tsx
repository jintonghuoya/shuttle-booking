import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import client from '../../api/client';
import type { Organization, Activity, OrgMember } from '../../api/types';

export default function OrgDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [org, setOrg] = useState<Organization | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [members, setMembers] = useState<OrgMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [addingMember, setAddingMember] = useState(false);

  const fetchAll = () => {
    if (!id) return;
    Promise.all([
      client.get(`/orgs/${id}`),
      client.get(`/orgs/${id}/activities`),
      client.get(`/orgs/${id}/members`),
    ])
      .then(([orgRes, actRes, memRes]) => {
        setOrg(orgRes.data.data);
        setActivities(actRes.data.data);
        setMembers(memRes.data.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchAll();
  }, [id]);

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;
    setAddingMember(true);
    try {
      await client.post(`/orgs/${id}/members`, { email: email.trim() });
      setEmail('');
      // Refresh members
      const res = await client.get(`/orgs/${id}/members`);
      setMembers(res.data.data);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add member');
    } finally {
      setAddingMember(false);
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;
  if (!org) return <div className="p-8 text-center text-gray-400">Organization not found</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <button onClick={() => navigate(-1)} className="text-blue-600 hover:underline mb-2 block">&larr; Back</button>
          <div className="flex items-start gap-4">
            {org.logoUrl ? (
              <img src={org.logoUrl} alt={org.name} className="w-16 h-16 rounded-lg object-cover" />
            ) : (
              <div className="w-16 h-16 rounded-lg bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xl">
                {org.name.charAt(0)}
              </div>
            )}
            <div>
              <h1 className="text-2xl font-bold">{org.name}</h1>
              {org.description && <p className="text-gray-500 mt-1">{org.description}</p>}
              <span className={org.isActive ? 'text-green-600 text-sm' : 'text-red-600 text-sm'}>
                {org.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6 space-y-6">
        {/* Members section */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h2 className="text-lg font-semibold mb-4">Members ({members.length})</h2>
          <div className="space-y-2 mb-4">
            {members.map(member => (
              <div key={member.id} className="flex justify-between items-center py-2 border-b border-gray-100 last:border-0">
                <div>
                  <p className="font-medium">{member.user.name}</p>
                  <p className="text-sm text-gray-500">{member.user.email}</p>
                </div>
                <span className="text-xs bg-gray-100 px-2 py-1 rounded">{member.role}</span>
              </div>
            ))}
          </div>

          {/* Add member form */}
          <form onSubmit={handleAddMember} className="flex gap-2">
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="Enter member email"
              className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
            <button
              type="submit"
              disabled={addingMember}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 transition disabled:opacity-50"
            >
              {addingMember ? 'Adding...' : 'Add'}
            </button>
          </form>
        </div>

        {/* Activities section */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">Activities</h2>
            <Link
              to={`/organizer/orgs/${id}/activities/new`}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 transition"
            >
              Create Activity
            </Link>
          </div>
          {activities.length === 0 ? (
            <p className="text-gray-400 text-center">No activities yet.</p>
          ) : (
            <div className="space-y-3">
              {activities.map(activity => (
                <div key={activity.id} className="border border-gray-200 rounded-lg p-3">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-medium">{activity.title}</h3>
                      <p className="text-sm text-gray-500">{activity.venue.name} - Court {activity.court.courtNumber}</p>
                      <p className="text-sm text-gray-500">
                        {activity.startDate} - {activity.endDate} | {String(activity.startHour).padStart(2, '0')}:00 - {String(activity.endHour).padStart(2, '0')}:00
                      </p>
                    </div>
                    <span className={`text-xs px-2 py-1 rounded ${
                      activity.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                    }`}>
                      {activity.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
