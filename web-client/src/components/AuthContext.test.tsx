import { render, screen, act } from '@testing-library/react';
import axios from 'axios';
import { AuthProvider, useAuth } from './AuthContext';

const STORAGE_KEY = 'ums.auth.token';

function makeJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

function makeTokenResponse(jwt: string) {
  return {
    access_token: jwt,
    expires_in: 300,
    refresh_expires_in: 1800,
    refresh_token: 'refresh',
    scope: 'openid',
    token_type: 'Bearer',
  };
}

const futureExp = Math.floor(Date.now() / 1000) + 600;
const pastExp = Math.floor(Date.now() / 1000) - 600;

const validJwt = makeJwt({
  exp: futureExp,
  preferred_username: 'admin',
  resource_access: { 'ums-client-webapp': { roles: ['admin'] } },
});

const expiredJwt = makeJwt({
  exp: pastExp,
  preferred_username: 'admin',
  resource_access: { 'ums-client-webapp': { roles: ['admin'] } },
});

let captured: ReturnType<typeof useAuth> | null = null;

const Probe = () => {
  captured = useAuth();
  return (
    <div>
      <span data-testid='auth'>{String(captured.isAuthenticated)}</span>
      <span data-testid='username'>{captured.username ?? ''}</span>
      <span data-testid='role'>{captured.role ?? ''}</span>
    </div>
  );
};

function renderProvider() {
  render(
    <AuthProvider>
      <Probe />
    </AuthProvider>,
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    captured = null;
    vi.restoreAllMocks();
  });

  it('starts unauthenticated when localStorage is empty', () => {
    renderProvider();

    expect(screen.getByTestId('auth')).toHaveTextContent('false');
    expect(screen.getByTestId('username')).toHaveTextContent('');
  });

  it('hydrates session from localStorage when stored token is valid', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(validJwt)));

    renderProvider();

    expect(screen.getByTestId('auth')).toHaveTextContent('true');
    expect(screen.getByTestId('username')).toHaveTextContent('admin');
    expect(screen.getByTestId('role')).toHaveTextContent('ADMIN');
  });

  it('clears storage and stays unauthenticated when stored token is expired', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(expiredJwt)));

    renderProvider();

    expect(screen.getByTestId('auth')).toHaveTextContent('false');
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('writes token to localStorage on successful login when remember is true', async () => {
    vi.spyOn(axios, 'post').mockResolvedValue({ data: makeTokenResponse(validJwt) });
    renderProvider();

    await act(async () => {
      await captured!.loginHandler({ username: 'admin', password: 'pw' }, true);
    });

    expect(localStorage.getItem(STORAGE_KEY)).not.toBeNull();
    expect(screen.getByTestId('auth')).toHaveTextContent('true');
    expect(screen.getByTestId('username')).toHaveTextContent('admin');
  });

  it('does not write to localStorage when remember is false', async () => {
    vi.spyOn(axios, 'post').mockResolvedValue({ data: makeTokenResponse(validJwt) });
    renderProvider();

    await act(async () => {
      await captured!.loginHandler({ username: 'admin', password: 'pw' }, false);
    });

    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(screen.getByTestId('auth')).toHaveTextContent('true');
    expect(screen.getByTestId('username')).toHaveTextContent('admin');
  });

  it('removes token from localStorage on logout', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(validJwt)));
    renderProvider();

    act(() => {
      captured!.logoutHandler();
    });

    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(screen.getByTestId('auth')).toHaveTextContent('false');
  });
});
