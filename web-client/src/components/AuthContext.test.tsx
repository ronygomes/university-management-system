import { render, screen, act } from '@testing-library/react';
import axios from 'axios';
import { AuthProvider, useAuth } from './AuthContext';
import * as AuthTokenModule from '../api/authToken';

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

  it('refreshes on mount when stored access token is expired but refresh_token is valid', async () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(expiredJwt)));
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({ data: makeTokenResponse(validJwt) });

    renderProvider();

    expect(await screen.findByTestId('auth')).toHaveTextContent('true');
    expect(screen.getByTestId('username')).toHaveTextContent('admin');
    expect(postSpy.mock.calls[0][1]).toMatchObject({ grant_type: 'refresh_token', refresh_token: 'refresh' });
    // remembered session is re-persisted with the refreshed token
    expect(localStorage.getItem(STORAGE_KEY)).not.toBeNull();
  });

  it('logs out and clears storage when the on-mount refresh fails', async () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(expiredJwt)));
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('invalid_grant'));

    renderProvider();

    expect(await screen.findByTestId('auth')).toHaveTextContent('false');
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('clears storage immediately when access token is expired and there is no refresh_token', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...makeTokenResponse(expiredJwt), refresh_token: '' }));

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

  it('pushes the token to the api auth interceptor on hydrate, login, and logout', async () => {
    const setApiTokenSpy = vi.spyOn(AuthTokenModule, 'setApiToken');

    localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(validJwt)));
    renderProvider();
    expect(setApiTokenSpy).toHaveBeenCalledWith(validJwt);

    setApiTokenSpy.mockClear();
    act(() => {
      captured!.logoutHandler();
    });
    expect(setApiTokenSpy).toHaveBeenCalledWith(null);

    vi.spyOn(axios, 'post').mockResolvedValue({ data: makeTokenResponse(validJwt) });
    setApiTokenSpy.mockClear();
    await act(async () => {
      await captured!.loginHandler({ username: 'admin', password: 'pw' }, false);
    });
    expect(setApiTokenSpy).toHaveBeenCalledWith(validJwt);
  });

  it('proactively refreshes the access token shortly before it expires', async () => {
    vi.useFakeTimers();
    try {
      const soonExp = Math.floor(Date.now() / 1000) + 120; // expires in 120s
      const soonJwt = makeJwt({
        exp: soonExp,
        preferred_username: 'admin',
        resource_access: { 'ums-client-webapp': { roles: ['admin'] } },
      });
      localStorage.setItem(STORAGE_KEY, JSON.stringify(makeTokenResponse(soonJwt)));
      const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({ data: makeTokenResponse(validJwt) });

      renderProvider();

      await act(async () => {
        await vi.advanceTimersByTimeAsync(61_000);
      });

      expect(postSpy).toHaveBeenCalledTimes(1);
      expect(postSpy.mock.calls[0][1]).toMatchObject({
        grant_type: 'refresh_token',
        refresh_token: 'refresh',
      });
    } finally {
      vi.useRealTimers();
    }
  });
});
