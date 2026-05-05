import { describe, it, expect, beforeEach } from 'vitest';

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

describe('API Client', () => {
  beforeEach(() => {
    localStorageMock.clear();
  });

  it('adds Authorization header when token exists', async () => {
    localStorageMock.setItem('token', 'test-jwt-token');

    const { default: client } = await import('../api/client');

    // Create a mock adapter to capture request config
    let capturedHeaders: Record<string, string> = {};
    client.interceptors.request.use((config) => {
      capturedHeaders = config.headers as Record<string, string>;
      return config;
    });

    try {
      await client.get('/test');
    } catch {
      // Expected to fail since no server
    }

    expect(capturedHeaders['Authorization']).toBe('Bearer test-jwt-token');
  });

  it('does not add Authorization header when no token', async () => {
    const { default: client } = await import('../api/client');

    let capturedHeaders: Record<string, string> = {};
    client.interceptors.request.use((config) => {
      capturedHeaders = config.headers as Record<string, string>;
      return config;
    });

    try {
      await client.get('/test');
    } catch {
      // Expected
    }

    expect(capturedHeaders['Authorization']).toBeUndefined();
  });
});
