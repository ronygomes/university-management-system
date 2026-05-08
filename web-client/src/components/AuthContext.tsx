import { useState, useContext, createContext, type ReactNode } from 'react';
import axios, { type AxiosResponse } from 'axios';
import { jwtDecode } from 'jwt-decode';
import { setApiToken } from '../api/authToken';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

type User = {
  username: string;
  password: string;
};

export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';

const TOKEN_ENDPOINT = `${import.meta.env.VITE_AUTH_SERVER_URL}/realms/ums/protocol/openid-connect/token`;
const STORAGE_KEY = 'ums.auth.token';
const ROLE_PRIORITY: Role[] = ['ADMIN', 'TEACHER', 'STUDENT'];
const CLIENT_ID = 'ums-client-webapp';

interface AuthContextType {
  isAuthenticated: boolean;
  token: AccessToken | null;
  username: string | null;
  role: Role | null;
  loginHandler: (user: User, remember: boolean) => Promise<boolean>;
  logoutHandler: () => void;
}

type AccessToken = {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  refresh_token: string;
  scope: string;
  token_type: string;
};

type DecodedAccessToken = {
  exp?: number;
  preferred_username?: string;
  resource_access?: Record<string, { roles?: string[] }>;
};

type HydratedSession = {
  token: AccessToken;
  username: string | null;
  role: Role | null;
};

function loadSession(): HydratedSession | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;

  try {
    const token = JSON.parse(raw) as AccessToken;
    const decoded = jwtDecode<DecodedAccessToken>(token.access_token);

    if (decoded.exp && decoded.exp * 1000 <= Date.now()) {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }

    return {
      token,
      username: decoded.preferred_username ?? null,
      role: extractRole(decoded),
    };
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

async function fetchAccessToken(user: User): Promise<AccessToken> {
  const response: AxiosResponse<AccessToken> = await axios.post<AccessToken>(
    TOKEN_ENDPOINT,
    {
      grant_type: 'password',
      username: user.username,
      password: user.password,
      client_id: CLIENT_ID,
      redirect_uri: 'http://localhost:3000/',
    },
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    },
  );

  return response.data;
}

function extractRole(decoded: DecodedAccessToken): Role | null {
  const roles = (decoded.resource_access?.[CLIENT_ID]?.roles ?? []).map((r) => r.toUpperCase());
  return ROLE_PRIORITY.find((r) => roles.includes(r)) ?? null;
}

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const initial = loadSession();
  if (initial) {
    setApiToken(initial.token.access_token);
  }
  const [isAuthenticated, setAuthenticated] = useState(initial !== null);
  const [token, setToken] = useState<AccessToken | null>(initial?.token ?? null);
  const [username, setUsername] = useState<string | null>(initial?.username ?? null);
  const [role, setRole] = useState<Role | null>(initial?.role ?? null);

  const loginHandler = async (user: User, remember: boolean): Promise<boolean> => {
    try {
      const data = await fetchAccessToken(user);
      const decoded = jwtDecode<DecodedAccessToken>(data.access_token);

      if (remember) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
      }
      setApiToken(data.access_token);
      setToken(data);
      setUsername(decoded.preferred_username ?? null);
      setRole(extractRole(decoded));
      setAuthenticated(true);
      return true;
    } catch (error) {
      // eslint-disable-line @typescript-eslint/no-unused-vars
      setAuthenticated(false);
      return false;
    }
  };

  const logoutHandler = () => {
    localStorage.removeItem(STORAGE_KEY);
    setApiToken(null);
    setAuthenticated(false);
    setToken(null);
    setUsername(null);
    setRole(null);
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, token, username, role, loginHandler, logoutHandler }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
};
