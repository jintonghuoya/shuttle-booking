import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client';
import type { Venue } from '../../api/types';

interface VenueWithApproval extends Venue {
  approvalStatus?: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export default function MyVenuesPage() {
  const [venues, setVenues] = useState<VenueWithApproval[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/venues/my')
      .then(res => setVenues(res.data.data))
      .catch(() => alert('Failed to load venues'))
      .finally(() => setLoading(false));
  }, []);

  const approvalBadge = (status?: string) => {
    switch (status) {
      case 'APPROVED':
        return <span className="text-green-600 font-medium">Approved</span>;
      case 'REJECTED':
        return <span className="text-red-600 font-medium">Rejected</span>;
      case 'PENDING':
      default:
        return <span className="text-yellow-600 font-medium">Pending</span>;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">My Venues</h1>
          <div className="flex gap-4">
            <Link to="/" className="text-blue-600 hover:underline">Home</Link>
            <Link to="/organizer/venues/new" className="text-blue-600 hover:underline">Submit New</Link>
          </div>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-4 py-6">
        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : venues.length === 0 ? (
          <p className="text-gray-500 text-center">You have not submitted any venues yet.</p>
        ) : (
          <div className="space-y-3">
            {venues.map(venue => (
              <div key={venue.id} className="bg-white rounded-lg shadow-sm p-4">
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-lg font-semibold">{venue.name}</h2>
                    <p className="text-sm text-gray-500">{venue.address}</p>
                  </div>
                  {approvalBadge(venue.approvalStatus)}
                </div>
                <div className="mt-2 flex items-center gap-2">
                  <span className={venue.active ? 'text-green-600 text-sm' : 'text-red-600 text-sm'}>
                    {venue.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <div className="mt-3 flex gap-3 text-sm">
                  <Link
                    to={`/organizer/venues/${venue.id}/edit`}
                    className="text-blue-600 hover:underline"
                  >
                    Edit Venue
                  </Link>
                  <Link
                    to={`/organizer/venues/${venue.id}/courts/new`}
                    className="text-blue-600 hover:underline"
                  >
                    Add Court
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
