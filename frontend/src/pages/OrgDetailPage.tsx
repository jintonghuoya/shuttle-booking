import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import client from '../api/client';
import type { Organization, Activity } from '../api/types';
import { useAuth } from '../context/AuthContext';

export default function OrgDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [org, setOrg] = useState<Organization | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [isFollowing, setIsFollowing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [followLoading, setFollowLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      client.get(`/orgs/${id}`),
      client.get(`/orgs/${id}/activities`),
    ])
      .then(([orgRes, actRes]) => {
        setOrg(orgRes.data.data);
        setActivities(actRes.data.data);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!id || !isAuthenticated) return;
    client.get(`/user/following`)
      .then(res => {
        const following: { org: Organization }[] = res.data.data;
        setIsFollowing(following.some(f => f.org.id === Number(id)));
      })
      .catch(() => {});
  }, [id, isAuthenticated]);

  const handleFollow = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setFollowLoading(true);
    try {
      await client.post(`/orgs/${id}/follow`);
      setIsFollowing(prev => !prev);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    } finally {
      setFollowLoading(false);
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
            <div className="flex-1">
              <h1 className="text-2xl font-bold">{org.name}</h1>
              {org.description && <p className="text-gray-500 mt-1">{org.description}</p>}
            </div>
            <button
              onClick={handleFollow}
              disabled={followLoading}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                isFollowing
                  ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  : 'bg-blue-600 text-white hover:bg-blue-700'
              }`}
            >
              {followLoading ? '...' : isFollowing ? 'Unfollow' : 'Follow'}
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        <h2 className="text-lg font-semibold mb-4">Activities</h2>
        {activities.length === 0 ? (
          <p className="text-gray-400 text-center">No activities yet.</p>
        ) : (
          <div className="space-y-3">
            {activities.map(activity => (
              <Link
                key={activity.id}
                to={`/activities/${activity.id}`}
                className="block bg-white rounded-lg shadow-sm p-4 hover:border-blue-300 border border-gray-200 transition"
              >
                <h3 className="font-medium text-blue-600">{activity.title}</h3>
                <p className="text-sm text-gray-500 mt-1">{activity.venueName}</p>
                <div className="flex gap-4 mt-2 text-sm text-gray-600">
                  <span>{activity.startDate} - {activity.endDate}</span>
                  <span>{String(activity.startHour).padStart(2, '0')}:00 - {String(activity.endHour).padStart(2, '0')}:00</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
