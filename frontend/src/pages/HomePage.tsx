import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { Venue } from '../api/types';

export default function HomePage() {
  const { isAuthenticated, logout, hasRole } = useAuth();
  const [venues, setVenues] = useState<Venue[]>([]);
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchVenues = useCallback(async (lat?: number, lng?: number) => {
    setLoading(true);
    try {
      if (lat && lng) {
        const res = await client.get(`/venues/nearby?lat=${lat}&lng=${lng}&radius=20`);
        setVenues(res.data.data);
      } else {
        const res = await client.get('/venues?page=0&size=50');
        setVenues(res.data.data.content);
      }
    } catch (err) {
      console.error('Failed to fetch venues', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const loc = { lat: pos.coords.latitude, lng: pos.coords.longitude };
          setUserLocation(loc);
          fetchVenues(loc.lat, loc.lng);
        },
        () => fetchVenues()
      );
    } else {
      fetchVenues();
    }
  }, [fetchVenues]);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <Link to="/" className="text-xl font-bold text-blue-600">Shuttle Booking</Link>
          <nav className="flex gap-4 items-center text-sm">
            {isAuthenticated && (
              <Link to="/following" className="text-gray-600 hover:text-blue-600">Following</Link>
            )}
            <Link to="/bookings" className="text-gray-600 hover:text-blue-600">My Bookings</Link>
            {isAuthenticated && (hasRole('ROLE_ORGANIZER') || hasRole('ROLE_ADMIN')) && (
              <Link to="/organizer/orgs" className="text-gray-600 hover:text-blue-600">My Orgs</Link>
            )}
            {isAuthenticated && hasRole('ROLE_ORGANIZER') && (
              <Link to="/organizer/venues" className="text-gray-600 hover:text-blue-600">My Venues</Link>
            )}
            {isAuthenticated && hasRole('ROLE_ADMIN') && (
              <Link to="/admin" className="text-gray-600 hover:text-blue-600">Admin</Link>
            )}
            {isAuthenticated ? (
              <>
                <Link to="/profile" className="text-gray-600 hover:text-blue-600">Profile</Link>
                <button onClick={logout} className="text-gray-600 hover:text-blue-600">Logout</button>
              </>
            ) : (
              <Link to="/login" className="text-gray-600 hover:text-blue-600">Login</Link>
            )}
          </nav>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6">
        {/* Map placeholder */}
        <div className="bg-white rounded-lg shadow-sm h-64 flex items-center justify-center text-gray-400 mb-6">
          <div className="text-center">
            <p className="text-lg font-medium">Map View</p>
            <p className="text-sm">Google Maps will render here</p>
            <p className="text-xs mt-2">Center: {userLocation ? `${userLocation.lat.toFixed(4)}, ${userLocation.lng.toFixed(4)}` : 'Singapore'}</p>
          </div>
        </div>

        {/* Venue list */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h2 className="text-lg font-semibold mb-4">
            Venues {userLocation && <span className="text-sm font-normal text-gray-500">(near you)</span>}
          </h2>
          {loading ? (
            <p className="text-gray-400">Loading...</p>
          ) : venues.length === 0 ? (
            <p className="text-gray-400">No venues found</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {venues.map((venue) => (
                <Link
                  key={venue.id}
                  to={`/venues/${venue.id}`}
                  className="block p-3 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-medium">{venue.name}</h3>
                      <p className="text-sm text-gray-500">{venue.address}</p>
                    </div>
                    {venue.distanceKm != null && (
                      <span className="text-sm text-blue-600 font-medium whitespace-nowrap">
                        {venue.distanceKm.toFixed(1)} km
                      </span>
                    )}
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
