import { createContext, useState, useContext, ReactNode, useEffect } from 'react';

interface AuthState {
  isAuthenticated: boolean;
  user: { username: string; roles: string[] } | null;
  loading: boolean; // Add loading state
  login: (user: { username: string; roles: string[] }) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<{ username: string; roles: string[] } | null>(null);
  const [loading, setLoading] = useState(true); // Initialize loading to true

  useEffect(() => {
    try {
      const storedUser = sessionStorage.getItem('user');
      if (storedUser) {
        setUser(JSON.parse(storedUser));
      }
    } finally {
      setLoading(false); // Set loading to false after checking session storage
    }
  }, []);

  const login = (userData: { username: string; roles: string[] }) => {
    setUser(userData);
    sessionStorage.setItem('user', JSON.stringify(userData));
  };

  const logout = async () => {
    try {
      const response = await fetch('/api/logout', { method: 'POST' });
      if (response.ok) {
        setUser(null);
        sessionStorage.removeItem('user');
      } else {
        console.error('Logout failed on server.');
      }
    } catch (error) {
      console.error('Logout request failed:', error);
    }
  };

  const authState: AuthState = {
    isAuthenticated: !!user,
    user,
    loading, // Expose loading state
    login,
    logout,
  };

  return <AuthContext.Provider value={authState}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
