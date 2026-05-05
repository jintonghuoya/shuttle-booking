export interface User {
  id: number;
  email: string;
  name: string;
  role: 'ROLE_USER' | 'ROLE_ORGANIZER' | 'ROLE_ADMIN';
  active: boolean;
}

export interface Venue {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  description: string;
  phone: string;
  active: boolean;
  submittedByName: string;
  distanceKm: number | null;
}

export interface Court {
  id: number;
  courtNumber: number;
  name: string;
  pricePerHourSgd: number;
  active: boolean;
}

export interface TimeSlot {
  id: number;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: 'AVAILABLE' | 'HELD' | 'BOOKED';
}

export interface Booking {
  id: number;
  bookingRef: string;
  status: 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'REFUNDED' | 'EXPIRED';
  totalAmount: number;
  venueName: string;
  courtName: string;
  courtNumber: number;
  slotDate: string;
  startTime: string;
  endTime: string;
  createdAt: string;
}

export interface Approval {
  id: number;
  venueId: number;
  venueName: string;
  submittedByName: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  reviewNote: string;
  reviewedByName: string;
  createdAt: string;
}

export interface Organization {
  id: number;
  name: string;
  description?: string;
  logoUrl?: string;
  createdByName: string;
  createdByUserId: number;
  active: boolean;
  createdAt: string;
}

export interface Activity {
  id: number;
  org: Organization;
  venueId: number;
  venueName: string;
  courtId: number;
  courtNumber: number;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  startHour: number;
  endHour: number;
  status: string;
  createdAt: string;
}

export interface OrgMember {
  id: number;
  user: User;
  role: string;
  createdAt: string;
}

export interface UserFollowing {
  id: number;
  org: Organization;
  createdAt: string;
}

export interface VenueFollowing {
  id: number;
  venue: Venue;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
