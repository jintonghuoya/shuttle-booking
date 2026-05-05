import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { Venue } from '../api/types';

// Fix Leaflet default marker icon path issue with bundlers
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Singapore default center
const SINGAPORE = { lat: 1.3521, lng: 103.8198 };

// Blue dot icon for user's current position
const userIcon = L.divIcon({
  className: '',
  html: `<div style="
    width: 16px; height: 16px;
    background: #3b82f6;
    border: 3px solid white;
    border-radius: 50%;
    box-shadow: 0 0 6px rgba(59,130,246,0.5);
  "></div>`,
  iconSize: [16, 16],
  iconAnchor: [8, 8],
});

function CenterUpdater({ center }: { center: { lat: number; lng: number } }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, map.getZoom());
  }, [center, map]);
  return null;
}

function LocateButton({ onLocate }: { onLocate: () => void }) {
  const map = useMap();
  return (
    <button
      onClick={() => { onLocate(); map.locate({ setView: true, maxZoom: 14 }); }}
      className="absolute top-2 right-2 z-[1000] bg-white rounded-lg shadow-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition flex items-center gap-1.5"
      title="Center on my location"
    >
      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
      My Location
    </button>
  );
}

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

  const navigate = useNavigate();
  const center = userLocation || SINGAPORE;

  const handleLocate = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const loc = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        setUserLocation(loc);
        fetchVenues(loc.lat, loc.lng);
      },
      () => alert('Unable to get your location')
    );
  };

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
        {/* Map */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden mb-6">
          <MapContainer
            center={[center.lat, center.lng]}
            zoom={13}
            className="h-64 w-full"
            scrollWheelZoom={false}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <CenterUpdater center={center} />
            <LocateButton onLocate={handleLocate} />
            {userLocation && (
              <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
                <Popup><span className="text-sm font-medium">You are here</span></Popup>
              </Marker>
            )}
            {venues.filter(v => v.latitude && v.longitude).map(venue => (
              <Marker
                key={venue.id}
                position={[venue.latitude, venue.longitude]}
                eventHandlers={{
                  click: () => navigate(`/venues/${venue.id}`),
                }}
              >
                <Popup>
                  <div className="text-sm">
                    <p className="font-medium">{venue.name}</p>
                    <p className="text-gray-500">{venue.address}</p>
                    {venue.distanceKm != null && (
                      <p className="text-blue-600 mt-1">{venue.distanceKm.toFixed(1)} km away</p>
                    )}
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
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
