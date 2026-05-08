import type { InternalAxiosRequestConfig } from 'axios';
import { setApiToken, authRequestInterceptor } from './authToken';

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
