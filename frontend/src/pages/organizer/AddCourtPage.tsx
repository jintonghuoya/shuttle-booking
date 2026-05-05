import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import client from '../../api/client';

export default function AddCourtPage() {
  const { venueId } = useParams<{ venueId: string }>();
  const { hasRole } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    courtNumber: '',
    name: '',
    pricePerHourSgd: '',
  });
  const [submitting, setSubmitting] = useState(false);

  if (!hasRole('ROLE_ORGANIZER')) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-red-600">Access denied. Organizer role required.</p>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await client.post(`/venues/${venueId}/courts`, {
        courtNumber: parseInt(form.courtNumber, 10),
        name: form.name,
        pricePerHourSgd: parseFloat(form.pricePerHourSgd),
      });
      navigate(`/organizer/venues/${venueId}`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add court');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <h1 className="text-xl font-bold">Add Court</h1>
        </div>
      </header>
      <main className="max-w-lg mx-auto px-4 py-6">
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Court Number</label>
            <input
              type="number"
              value={form.courtNumber}
              onChange={e => setForm({ ...form, courtNumber: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
              min={1}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Court Name</label>
            <input
              type="text"
              value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Price Per Hour (SGD)</label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={form.pricePerHourSgd}
              onChange={e => setForm({ ...form, pricePerHourSgd: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition disabled:opacity-50"
          >
            {submitting ? 'Adding...' : 'Add Court'}
          </button>
        </form>
      </main>
    </div>
  );
}
