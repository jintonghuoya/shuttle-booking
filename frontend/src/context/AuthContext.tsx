import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import client from '../api/client';
import type { User } from '../api/types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string, role?: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType>(null!);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));

  useEffect(() => {
    if (token) {
      client.get('/auth/me')
        .then(res => {
          setUser(res.data.data);
          localStorage.setItem('user', JSON.stringify(res.data.data));
        })
        .catch(() => {
          logout();
        });
    }
  }, []);

  const login = async (email: string, password: string) => {
    const res = await client.post('/auth/login', { email, password });
    const { token: newToken, user: newUser } = res.data.data;
    setToken(newToken);
    setUser(newUser);
    localStorage.setItem('token', newToken);
    localStorage.setItem('user', JSON.stringify(newUser));
  };

  const register = async (email: string, password: string, name: string, role?: string) => {
    const res = await client.post('/auth/register', { email, password, name, role });
    const { token: newToken, user: newUser } = res.data.data;
    setToken(newToken);
    setUser(newUser);
    localStorage.setItem('token', newToken);
    localStorage.setItem('user', JSON.stringify(newUser));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{
      user, token, login, register, logout,
      isAuthenticated: !!token,
      hasRole: (role: string) => user?.role === role
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
