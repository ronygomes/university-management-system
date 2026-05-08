import { useState, useContext, createContext, type ReactNode } from 'react';
import axios, { type AxiosResponse } from 'axios';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

type User = {
  username: string;
  password: string;
};

export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';

const ROLE_PRIORITY: Role[] = ['ADMIN', 'TEACHER', 'STUDENT'];
const CLIENT_ID = 'ums-client-webapp';

interface AuthContextType {
  isAuthenticated: boolean;
  token: AccessToken | null;
  username: string | null;
  role: Role | null;
  loginHandler: (user: User) => Promise<boolean>;
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
  preferred_username?: string;
  resource_access?: Record<string, { roles?: string[] }>;
};

const TOKEN_ENDPOINT = `${import.meta.env.VITE_AUTH_SERVER_URL}/realms/ums/protocol/openid-connect/token`;

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
  const [isAuthenticated, setAuthenticated] = useState(false);
  const [token, setToken] = useState<AccessToken | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [role, setRole] = useState<Role | null>(null);

  const loginHandler = async (user: User): Promise<boolean> => {
    try {
      const data = await fetchAccessToken(user);
      const decoded = jwtDecode<DecodedAccessToken>(data.access_token);

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
