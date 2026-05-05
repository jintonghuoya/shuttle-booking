import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider, useAuth } from '../context/AuthContext';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
  };
})();
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock });

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: () => ({
      get: vi.fn().mockRejectedValue(new Error('No server')),
      post: vi.fn().mockRejectedValue(new Error('No server')),
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    }),
  },
}));

function TestComponent() {
  const { isAuthenticated, user, hasRole } = useAuth();
  return (
    <div>
      <span data-testid="authenticated">{isAuthenticated.toString()}</span>
      <span data-testid="user-name">{user?.name || 'none'}</span>
      <span data-testid="is-admin">{hasRole('ROLE_ADMIN').toString()}</span>
    </div>
  );
}

function renderWithAuth() {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    </BrowserRouter>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorageMock.clear();
  });

  it('starts unauthenticated when no token', () => {
    renderWithAuth();
    expect(screen.getByTestId('authenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('user-name')).toHaveTextContent('none');
  });

  it('starts authenticated when token in localStorage', async () => {
    localStorageMock.setItem('token', 'some-token');
    localStorageMock.setItem('user', JSON.stringify({ id: 1, name: 'Test User', role: 'ROLE_USER' }));

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('user-name').textContent).not.toBe('none');
    });
  });

  it('hasRole returns correct value', async () => {
    localStorageMock.setItem('token', 'some-token');
    localStorageMock.setItem('user', JSON.stringify({ id: 1, name: 'Admin', role: 'ROLE_ADMIN' }));

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('is-admin')).toHaveTextContent('true');
    });
  });
});
