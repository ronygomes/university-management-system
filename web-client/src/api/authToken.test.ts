import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import {
  setApiToken,
  setUnauthorizedHandler,
  authRequestInterceptor,
  authResponseErrorInterceptor,
} from './authToken';

function makeConfig(): InternalAxiosRequestConfig {
  return { headers: {} } as InternalAxiosRequestConfig;
}

describe('authToken', () => {
  beforeEach(() => {
    setApiToken(null);
  });

  it('adds Authorization header when token is set', () => {
    setApiToken('abc');
    const config = authRequestInterceptor(makeConfig());

    expect(config.headers.Authorization).toBe('Bearer abc');
  });

  it('does not add Authorization header when token is null', () => {
    setApiToken(null);
    const config = authRequestInterceptor(makeConfig());

    expect(config.headers.Authorization).toBeUndefined();
  });

  it('reflects token updates on subsequent requests', () => {
    setApiToken('first');
    expect(authRequestInterceptor(makeConfig()).headers.Authorization).toBe('Bearer first');

    setApiToken('second');
    expect(authRequestInterceptor(makeConfig()).headers.Authorization).toBe('Bearer second');

    setApiToken(null);
    expect(authRequestInterceptor(makeConfig()).headers.Authorization).toBeUndefined();
  });
});

function makeError(status: number, url: string): AxiosError {
  return {
    response: { status } as AxiosError['response'],
    config: { url } as AxiosError['config'],
  } as AxiosError;
}

describe('authResponseErrorInterceptor', () => {
  beforeEach(() => {
    setUnauthorizedHandler(null);
  });

  it('calls the registered handler on 401 from a /v1/ URL', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);

    await expect(authResponseErrorInterceptor(makeError(401, '/v1/courses'))).rejects.toBeDefined();
    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('does NOT call the handler on 401 from a Keycloak /realms/ URL (login failure)', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);

    await expect(
      authResponseErrorInterceptor(makeError(401, 'http://localhost:8000/realms/ums/protocol/openid-connect/token')),
    ).rejects.toBeDefined();
    expect(handler).not.toHaveBeenCalled();
  });

  it('does NOT call the handler when status is not 401', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);

    await expect(authResponseErrorInterceptor(makeError(403, '/v1/courses'))).rejects.toBeDefined();
    await expect(authResponseErrorInterceptor(makeError(500, '/v1/courses'))).rejects.toBeDefined();
    expect(handler).not.toHaveBeenCalled();
  });

  it('does not throw when no handler is registered', async () => {
    setUnauthorizedHandler(null);
    await expect(authResponseErrorInterceptor(makeError(401, '/v1/courses'))).rejects.toBeDefined();
  });

  it('always rejects so caller logic still runs', async () => {
    setUnauthorizedHandler(() => {});
    const e = makeError(401, '/v1/courses');
    await expect(authResponseErrorInterceptor(e)).rejects.toBe(e);
  });
});
