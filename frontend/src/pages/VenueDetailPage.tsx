import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { Venue, Activity, Court } from '../api/types';

export default function VenueDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [venue, setVenue] = useState<Venue | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [courts, setCourts] = useState<Court[]>([]);
  const [followedCourtIds, setFollowedCourtIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      client.get(`/venues/${id}`),
      client.get(`/venues/${id}/activities`),
      client.get(`/venues/${id}/courts`),
    ]).then(([venueRes, activitiesRes, courtsRes]) => {
      setVenue(venueRes.data.data);
      setActivities(activitiesRes.data.data || []);
      setCourts(courtsRes.data.data || []);
    }).finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!isAuthenticated || courts.length === 0) return;
    Promise.all(
      courts.map(c => client.get(`/user/following/courts/${c.id}/check`).then(res => ({ id: c.id, following: res.data.data })))
    ).then(results => {
      setFollowedCourtIds(new Set(results.filter(r => r.following).map(r => r.id)));
    }).catch(() => {});
  }, [isAuthenticated, courts]);

  const handleToggleCourtFollow = async (courtId: number) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    const isCurrentlyFollowed = followedCourtIds.has(courtId);
    if (isCurrentlyFollowed) {
      if (!confirm('Unfollow this court?')) return;
    }
    try {
      const res = await client.post(`/user/following/courts/${courtId}/toggle`);
      setFollowedCourtIds(prev => {
        const next = new Set(prev);
        if (res.data.data) next.add(courtId);
        else next.delete(courtId);
        return next;
      });
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;
  if (!venue) return <div className="p-8 text-center text-gray-400">Venue not found</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <button onClick={() => navigate(-1)} className="text-blue-600 hover:underline mb-2 block">&larr; Back</button>
          <h1 className="text-2xl font-bold">{venue.name}</h1>
          <p className="text-gray-500">{venue.address}</p>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 space-y-6">
        {/* Location map */}
        {venue.latitude && venue.longitude && (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <MapContainer
              center={[venue.latitude, venue.longitude]}
              zoom={15}
              className="h-64 w-full"
              scrollWheelZoom={false}
              dragging={false}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <Marker position={[venue.latitude, venue.longitude]}>
                <Popup>
                  <div className="text-sm">
                    <p className="font-medium">{venue.name}</p>
                    <p className="text-gray-500">{venue.address}</p>
                  </div>
                </Popup>
              </Marker>
            </MapContainer>
          </div>
        )}

        {venue.description && <p className="text-gray-600">{venue.description}</p>}
        {venue.phone && <p className="text-gray-600">Phone: {venue.phone}</p>}

        {/* Courts */}
        {courts.length > 0 && (
          <div className="bg-white rounded-lg shadow-sm p-4">
            <h2 className="text-lg font-semibold mb-4">Courts ({courts.length})</h2>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              {courts.map(court => (
                <div key={court.id} className="border border-gray-200 rounded-lg p-3 flex justify-between items-center">
                  <div>
                    <p className="font-medium">Court {court.courtNumber}</p>
                    <p className="text-sm text-gray-500">${court.pricePerHourSgd}/hr</p>
                  </div>
                  <button
                    onClick={() => handleToggleCourtFollow(court.id)}
                    className="text-xl transition hover:scale-110"
                    title={followedCourtIds.has(court.id) ? 'Unfollow court' : 'Follow court'}
                  >
                    {followedCourtIds.has(court.id) ? '❤️' : '\u{1F5A4}'}
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Activities */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h2 className="text-lg font-semibold mb-4">Activities at this Venue</h2>
          {activities.length === 0 ? (
            <p className="text-gray-400 text-center">No activities published at this venue yet</p>
          ) : (
            <div className="space-y-3">
              {activities.map(activity => (
                <Link
                  key={activity.id}
                  to={`/activities/${activity.id}`}
                  className="block border border-gray-200 rounded-lg p-4 hover:shadow-md transition"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-medium text-blue-600">{activity.title}</h3>
                      <p className="text-sm text-gray-500 mt-1">
                        {activity.org?.name}{activity.courtNumber ? ` · Court ${activity.courtNumber}` : ''}
                      </p>
                      <p className="text-sm text-gray-500">
                        {activity.startDate} ~ {activity.endDate} · {activity.startHour}:00 - {activity.endHour}:00
                      </p>
                    </div>
                    <span className="text-sm text-blue-600 font-medium">&rarr;</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
