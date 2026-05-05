import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import client from '../api/client';
import type { Booking } from '../api/types';
import { useAuth } from '../context/AuthContext';

const statusColors: Record<string, string> = {
  PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
  REFUNDED: 'bg-blue-100 text-blue-800',
  EXPIRED: 'bg-red-100 text-red-800',
};

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const { isAuthenticated } = useAuth();
  const [searchParams] = useSearchParams();
  const paymentSuccess = searchParams.get('payment') === 'success';
  const paymentCancelled = searchParams.get('payment') === 'cancelled';

  useEffect(() => {
    if (paymentSuccess || paymentCancelled) {
      window.history.replaceState({}, '', '/bookings');
    }
  }, [paymentSuccess, paymentCancelled]);

  useEffect(() => {
    if (!isAuthenticated) return;
    client.get('/bookings/mine')
      .then(res => setBookings(res.data.data))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await client.post(`/bookings/${id}/cancel`);
      setBookings(prev => prev.map(b => b.id === id ? { ...b, status: 'CANCELLED' as const } : b));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Cancel failed');
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Please <Link to="/login" className="text-blue-600">sign in</Link> to view your bookings.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">My Bookings</h1>
          <Link to="/" className="text-blue-600 hover:underline">Home</Link>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {paymentSuccess && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-4 text-green-700">
            Payment successful! Your booking is confirmed.
          </div>
        )}

        {paymentCancelled && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4 text-yellow-700">
            Payment was cancelled. Your booking is still pending payment.
          </div>
        )}

        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : bookings.length === 0 ? (
          <p className="text-gray-400 text-center">No bookings yet.</p>
        ) : (
          <div className="space-y-3">
            {bookings.map(booking => (
              <div key={booking.id} className="bg-white rounded-lg shadow-sm p-4">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-medium">{booking.venueName}</h3>
                    <p className="text-sm text-gray-500">
                      {booking.courtDescription ? `${booking.courtDescription} | ` : ''}{booking.slotDate} {booking.startTime?.slice(0, 5)} - {booking.endTime?.slice(0, 5)}
                    </p>
                    <p className="text-sm text-gray-500">Ref: {booking.bookingRef.slice(0, 8)}...</p>
                  </div>
                  <div className="text-right">
                    <span className={`inline-block px-2 py-1 rounded text-xs font-medium ${statusColors[booking.status] || ''}`}>
                      {booking.status.replace(/_/g, ' ')}
                    </span>
                    <p className="text-sm font-medium mt-1">${booking.totalAmount}</p>
                    {(booking.status === 'PENDING_PAYMENT' || booking.status === 'CONFIRMED') && (
                      <button
                        onClick={() => handleCancel(booking.id)}
                        className="text-sm text-red-600 hover:underline mt-1"
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
