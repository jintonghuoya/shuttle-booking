import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import client from '../api/client';
import type { Activity, TimeSlot } from '../api/types';
import { useAuth } from '../context/AuthContext';
import dayjs from 'dayjs';

export default function ActivityDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [activity, setActivity] = useState<Activity | null>(null);
  const [slots, setSlots] = useState<TimeSlot[]>([]);
  const [selectedDate, setSelectedDate] = useState('');
  const [loading, setLoading] = useState(true);
  const [bookingSlotId, setBookingSlotId] = useState<number | null>(null);

  useEffect(() => {
    if (!id) return;
    client.get(`/activities/${id}`)
      .then(res => {
        const act: Activity = res.data.data;
        setActivity(act);
        setSelectedDate(act.startDate);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!id || !selectedDate) return;
    client.get(`/activities/${id}/slots?date=${selectedDate}`)
      .then(res => setSlots(res.data.data));
  }, [id, selectedDate]);

  const handleBook = async (slot: TimeSlot) => {
    if (!isAuthenticated || !id) {
      navigate('/login');
      return;
    }
    setBookingSlotId(slot.id);
    try {
      const res = await client.post('/bookings', {
        activityId: Number(id),
        timeSlotId: slot.id,
      });
      const booking = res.data.data;
      const payRes = await client.post(`/payments/checkout?bookingId=${booking.id}`);
      window.location.href = payRes.data.data;
    } catch (err: any) {
      alert(err.response?.data?.message || 'Booking failed');
      setBookingSlotId(null);
    }
  };

  // Generate date tabs from activity date range
  const dateTabs: dayjs.Dayjs[] = [];
  if (activity) {
    let current = dayjs(activity.startDate);
    const end = dayjs(activity.endDate);
    while (current.isBefore(end) || current.isSame(end, 'day')) {
      dateTabs.push(current);
      current = current.add(1, 'day');
    }
  }

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;
  if (!activity) return <div className="p-8 text-center text-gray-400">Activity not found</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <button onClick={() => navigate(-1)} className="text-blue-600 hover:underline mb-2 block">&larr; Back</button>
          <h1 className="text-2xl font-bold">{activity.title}</h1>
          {activity.description && <p className="text-gray-500 mt-1">{activity.description}</p>}
          <div className="flex gap-4 mt-2 text-sm text-gray-600">
            <span>By {activity.org.name}</span>
            <span>{activity.venueName}</span>
            {activity.courtNumber && <span>Court {activity.courtNumber}</span>}
          </div>
          <p className="text-sm text-gray-600 mt-1">
            {activity.startDate} - {activity.endDate} | {String(activity.startHour).padStart(2, '0')}:00 - {String(activity.endHour).padStart(2, '0')}:00
          </p>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {/* Date selector tabs */}
        <div className="flex gap-2 mb-4 overflow-x-auto">
          {dateTabs.map(d => {
            const dateStr = d.format('YYYY-MM-DD');
            return (
              <button
                key={dateStr}
                onClick={() => setSelectedDate(dateStr)}
                className={`px-4 py-2 rounded-lg text-sm whitespace-nowrap transition ${
                  selectedDate === dateStr
                    ? 'bg-blue-600 text-white'
                    : 'bg-white border border-gray-200 hover:border-blue-300'
                }`}
              >
                <div className="font-medium">{d.format('ddd')}</div>
                <div>{d.format('MMM D')}</div>
              </button>
            );
          })}
        </div>

        {/* Time slots grid */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h2 className="text-lg font-semibold mb-4">Time Slots</h2>
          {slots.length === 0 ? (
            <p className="text-gray-400">No slots available for this date</p>
          ) : (
            <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-2">
              {slots.map(slot => (
                <button
                  key={slot.id}
                  onClick={() => slot.status === 'AVAILABLE' && handleBook(slot)}
                  disabled={slot.status !== 'AVAILABLE' || bookingSlotId === slot.id}
                  className={`p-3 rounded-lg text-sm font-medium transition ${
                    slot.status === 'AVAILABLE'
                      ? 'bg-green-50 border border-green-200 hover:bg-green-100 text-green-700 cursor-pointer'
                      : slot.status === 'HELD'
                      ? 'bg-yellow-50 border border-yellow-200 text-yellow-700 cursor-not-allowed'
                      : 'bg-red-50 border border-red-200 text-red-700 cursor-not-allowed'
                  }`}
                >
                  {slot.startTime.slice(0, 5)}
                  <div className="text-xs mt-1">
                    {bookingSlotId === slot.id
                      ? 'Booking...'
                      : slot.status === 'AVAILABLE'
                      ? 'Book'
                      : slot.status === 'HELD'
                      ? 'Held'
                      : 'Booked'}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
