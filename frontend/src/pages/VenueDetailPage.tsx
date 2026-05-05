import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import client from '../api/client';
import type { Venue, Activity } from '../api/types';

export default function VenueDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [venue, setVenue] = useState<Venue | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      client.get(`/venues/${id}`),
      client.get(`/venues/${id}/activities`)
    ]).then(([venueRes, activitiesRes]) => {
      setVenue(venueRes.data.data);
      setActivities(activitiesRes.data.data || []);
    }).finally(() => setLoading(false));
  }, [id]);

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

      <main className="max-w-7xl mx-auto px-4 py-6">
        {venue.description && <p className="mb-4 text-gray-600">{venue.description}</p>}
        {venue.phone && <p className="mb-4 text-gray-600">Phone: {venue.phone}</p>}

        <h2 className="text-lg font-semibold mb-4">Activities at this Venue</h2>
        {activities.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-8 text-center text-gray-400">
            No activities published at this venue yet
          </div>
        ) : (
          <div className="space-y-3">
            {activities.map(activity => (
              <Link
                key={activity.id}
                to={`/activities/${activity.id}`}
                className="block bg-white rounded-lg shadow-sm p-4 hover:shadow-md transition"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-medium text-blue-600">{activity.title}</h3>
                    <p className="text-sm text-gray-500 mt-1">
                      {activity.org?.name} &middot; Court {activity.court?.courtNumber}
                    </p>
                    <p className="text-sm text-gray-500">
                      {activity.startDate} ~ {activity.endDate} &middot; {activity.startHour}:00 - {activity.endHour}:00
                    </p>
                  </div>
                  <span className="text-sm text-blue-600 font-medium">&rarr;</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
