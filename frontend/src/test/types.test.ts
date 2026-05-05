import { describe, it, expectTypeOf } from 'vitest';
import type { User, Venue, Booking, ApiResponse } from '../api/types';

describe('API types', () => {
  it('User has correct shape', () => {
    const user: User = {
      id: 1,
      email: 'test@test.com',
      name: 'Test',
      role: 'ROLE_USER',
      active: true,
    };
    expectTypeOf(user.id).toBeNumber();
    expectTypeOf(user.email).toBeString();
    expectTypeOf(user.role).toMatchTypeOf<'ROLE_USER' | 'ROLE_ORGANIZER' | 'ROLE_ADMIN'>();
  });

  it('Venue has correct shape', () => {
    const venue: Venue = {
      id: 1,
      name: 'Test Venue',
      address: '123 Singapore',
      latitude: 1.35,
      longitude: 103.82,
      description: 'desc',
      phone: '+651234',
      active: true,
      submittedByName: 'Org',
      distanceKm: 2.5,
      numberOfCourts: 6,
    };
    expectTypeOf(venue.latitude).toBeNumber();
    expectTypeOf(venue.distanceKm).toEqualTypeOf<number | null>();
  });

  it('Booking status is correct enum', () => {
    const booking: Booking = {
      id: 1,
      bookingRef: 'abc',
      status: 'CONFIRMED',
      totalAmount: 15,
      venueName: 'V',
      courtDescription: 'Court 3',
      slotDate: '2026-05-05',
      startTime: '10:00',
      endTime: '11:00',
      createdAt: '',
    };
    expectTypeOf(booking.status).toMatchTypeOf<'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'REFUNDED' | 'EXPIRED'>();
  });

  it('ApiResponse is generic', () => {
    const res: ApiResponse<User> = {
      success: true,
      message: 'ok',
      data: { id: 1, email: 'a@b.com', name: 'A', role: 'ROLE_USER', active: true },
    };
    expectTypeOf(res.data).toMatchTypeOf<User>();
  });
});
