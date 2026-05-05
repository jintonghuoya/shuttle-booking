import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import MyBookingsPage from '../pages/MyBookingsPage';

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: false,
    user: null,
  }),
}));

describe('MyBookingsPage', () => {
  it('shows sign in prompt when not authenticated', () => {
    render(
      <BrowserRouter>
        <MyBookingsPage />
      </BrowserRouter>
    );

    expect(screen.getByText('sign in')).toBeInTheDocument();
  });
});
