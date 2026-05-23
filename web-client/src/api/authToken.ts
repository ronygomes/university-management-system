import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';

let currentToken: string | null = null;
let unauthorizedHandler: (() => void) | null = null;

export function setApiToken(token: string | null): void {
  currentToken = token;
}

export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

export function authRequestInterceptor(
  config: InternalAxiosRequestConfig,
): InternalAxiosRequestConfig {
  if (currentToken && config.headers) {
    config.headers.Authorization = `Bearer ${currentToken}`;
  }
  return config;
}

export function authResponseErrorInterceptor(error: AxiosError): Promise<never> {
  const status = error.response?.status;
  const url = String(error.config?.url ?? '');
  // Skip Keycloak token endpoint — a 401 there is a bad-credentials login
  // failure, not an expired-session situation.
  if (status === 401 && !url.includes('/realms/') && unauthorizedHandler) {
    unauthorizedHandler();
  }
  return Promise.reject(error);
}

axios.interceptors.request.use(authRequestInterceptor);
axios.interceptors.response.use((r) => r, authResponseErrorInterceptor);
