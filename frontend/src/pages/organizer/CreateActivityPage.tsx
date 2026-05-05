import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import client from '../../api/client';
import type { Venue, Court } from '../../api/types';

export default function CreateActivityPage() {
  const { orgId } = useParams<{ orgId: string }>();
  const navigate = useNavigate();

  const [venues, setVenues] = useState<Venue[]>([]);
  const [courts, setCourts] = useState<Court[]>([]);
  const [selectedVenue, setSelectedVenue] = useState<number | null>(null);
  const [selectedCourt, setSelectedCourt] = useState<number | null>(null);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [startHour, setStartHour] = useState(9);
  const [endHour, setEndHour] = useState(21);

  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    client.get('/venues?page=0&size=100')
      .then(res => setVenues(res.data.data.content || res.data.data));
  }, []);

  useEffect(() => {
    if (!selectedVenue) {
      setCourts([]);
      setSelectedCourt(null);
      return;
    }
    client.get(`/venues/${selectedVenue}/courts`)
      .then(res => {
        setCourts(res.data.data);
        setSelectedCourt(null);
      });
  }, [selectedVenue]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!orgId || !selectedVenue || !selectedCourt) return;
    setSubmitting(true);
    try {
      await client.post('/activities', {
        orgId: Number(orgId),
        venueId: selectedVenue,
        courtId: selectedCourt,
        title,
        description,
        startDate,
        endDate,
        startHour,
        endHour,
      });
      navigate(`/organizer/orgs/${orgId}`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create activity');
      setSubmitting(false);
    }
  };

  const hours = Array.from({ length: 24 }, (_, i) => i);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <button onClick={() => navigate(-1)} className="text-blue-600 hover:underline mb-2 block">&larr; Back</button>
          <h1 className="text-2xl font-bold">Create Activity</h1>
        </div>
      </header>

      <main className="max-w-lg mx-auto px-4 py-6">
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
            <input
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Activity title"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              value={description}
              onChange={e => setDescription(e.target.value)}
              rows={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Activity description"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Venue</label>
            <select
              value={selectedVenue ?? ''}
              onChange={e => setSelectedVenue(Number(e.target.value) || null)}
              required
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select a venue</option>
              {venues.map(v => (
                <option key={v.id} value={v.id}>{v.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Court</label>
            <select
              value={selectedCourt ?? ''}
              onChange={e => setSelectedCourt(Number(e.target.value) || null)}
              required
              disabled={!selectedVenue}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
            >
              <option value="">Select a court</option>
              {courts.map(c => (
                <option key={c.id} value={c.id}>Court {c.courtNumber} - ${c.pricePerHourSgd}/hr</option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
              <input
                type="date"
                value={startDate}
                onChange={e => setStartDate(e.target.value)}
                required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
              <input
                type="date"
                value={endDate}
                onChange={e => setEndDate(e.target.value)}
                required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Hour</label>
              <select
                value={startHour}
                onChange={e => setStartHour(Number(e.target.value))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {hours.map(h => (
                  <option key={h} value={h}>{String(h).padStart(2, '0')}:00</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Hour</label>
              <select
                value={endHour}
                onChange={e => setEndHour(Number(e.target.value))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {hours.map(h => (
                  <option key={h} value={h}>{String(h).padStart(2, '0')}:00</option>
                ))}
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
          >
            {submitting ? 'Creating...' : 'Create Activity'}
          </button>
        </form>
      </main>
    </div>
  );
}
