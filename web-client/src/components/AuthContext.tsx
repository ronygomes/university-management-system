import { useState, useEffect, useContext, createContext, type ReactNode } from 'react';
import axios, { type AxiosResponse } from 'axios';
import { jwtDecode } from 'jwt-decode';
import { setApiToken, setUnauthorizedHandler } from '../api/authToken';

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
const REFRESH_GRACE_PERIOD_MS = 60_000;

interface AuthContextType {
  isAuthenticated: boolean;
  token: AccessToken | null;
  username: string | null;
  email: string | null;
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
  email?: string;
  resource_access?: Record<string, { roles?: string[] }>;
};

type HydratedSession = {
  token: AccessToken;
  username: string | null;
  email: string | null;
  role: Role | null;
};

type InitResult = {
  session: HydratedSession | null;
  staleToken: AccessToken | null;
};

function extractRole(decoded: DecodedAccessToken): Role | null {
  const roles = (decoded.resource_access?.[CLIENT_ID]?.roles ?? []).map((r) => r.toUpperCase());
  return ROLE_PRIORITY.find((r) => roles.includes(r)) ?? null;
}

function sessionFromToken(token: AccessToken, decoded: DecodedAccessToken): HydratedSession {
  return {
    token,
    username: decoded.preferred_username ?? null,
    email: decoded.email ?? null,
    role: extractRole(decoded),
  };
}

function isExpired(decoded: DecodedAccessToken): boolean {
  return decoded.exp ? decoded.exp * 1000 <= Date.now() : false;
}

function msUntilRefresh(accessToken: string): number {
  try {
    const { exp } = jwtDecode<DecodedAccessToken>(accessToken);
    if (!exp) return 0;
    return exp * 1000 - Date.now() - REFRESH_GRACE_PERIOD_MS;
  } catch {
    return 0;
  }
}

function computeInit(): InitResult {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return { session: null, staleToken: null };

  try {
    const token = JSON.parse(raw) as AccessToken;
    const decoded = jwtDecode<DecodedAccessToken>(token.access_token);

    if (!isExpired(decoded)) {
      return { session: sessionFromToken(token, decoded), staleToken: null };
    }
    if (token.refresh_token) {
      return { session: null, staleToken: token };
    }
    localStorage.removeItem(STORAGE_KEY);
    return { session: null, staleToken: null };

  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return { session: null, staleToken: null };
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

async function refreshAccessToken(refreshToken: string): Promise<AccessToken> {
  const response: AxiosResponse<AccessToken> = await axios.post<AccessToken>(
    TOKEN_ENDPOINT,
    {
      grant_type: 'refresh_token',
      client_id: CLIENT_ID,
      refresh_token: refreshToken,
    },
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    },
  );

  return response.data;
}

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [init] = useState<InitResult>(() => {
    const result = computeInit();
    if (result.session) {
      setApiToken(result.session.token.access_token);
    }
    return result;
  });

  const [initializing, setInitializing] = useState(init.staleToken !== null);
  const [isAuthenticated, setAuthenticated] = useState(init.session !== null);
  const [token, setToken] = useState<AccessToken | null>(init.session?.token ?? null);
  const [username, setUsername] = useState<string | null>(init.session?.username ?? null);
  const [email, setEmail] = useState<string | null>(init.session?.email ?? null);
  const [role, setRole] = useState<Role | null>(init.session?.role ?? null);

  const applySession = (data: AccessToken) => {
    const decoded = jwtDecode<DecodedAccessToken>(data.access_token);
    if (localStorage.getItem(STORAGE_KEY)) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    }
    setApiToken(data.access_token);
    setToken(data);
    setUsername(decoded.preferred_username ?? null);
    setEmail(decoded.email ?? null);
    setRole(extractRole(decoded));
    setAuthenticated(true);
  };

  const loginHandler = async (user: User, remember: boolean): Promise<boolean> => {
    try {
      const data = await fetchAccessToken(user);
      if (remember) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
      }
      applySession(data);
      return true;
    } catch {
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
    setEmail(null);
    setRole(null);
  };

  const runRefresh = async (refreshToken: string): Promise<void> => {
    try {
      const data = await refreshAccessToken(refreshToken);
      applySession(data);
    } catch {
      logoutHandler();
    }
  };

  useEffect(() => {
    setUnauthorizedHandler(() => logoutHandler());
    return () => setUnauthorizedHandler(null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!token) return;
    const delay = Math.max(0, msUntilRefresh(token.access_token));
    const timerId = setTimeout(() => {
      runRefresh(token.refresh_token);
    }, delay);
    return () => clearTimeout(timerId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  useEffect(() => {
    if (init.staleToken === null) return;
    let cancelled = false;
    (async () => {
      await runRefresh(init.staleToken!.refresh_token);
      if (!cancelled) setInitializing(false);
    })();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, token, username, email, role, loginHandler, logoutHandler }}
    >
      {initializing ? null : children}
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
