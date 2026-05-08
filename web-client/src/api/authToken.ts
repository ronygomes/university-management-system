import axios, { type InternalAxiosRequestConfig } from 'axios';

let currentToken: string | null = null;

export function setApiToken(token: string | null): void {
  currentToken = token;
}

export function authRequestInterceptor(
  config: InternalAxiosRequestConfig,
): InternalAxiosRequestConfig {
  if (currentToken && config.headers) {
    config.headers.Authorization = `Bearer ${currentToken}`;
  }
  return config;
}

axios.interceptors.request.use(authRequestInterceptor);
