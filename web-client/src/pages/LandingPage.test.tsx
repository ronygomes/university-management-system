import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import LandingPage from './LandingPage';
import * as AuthContext from '../components/AuthContext';
import type { Role } from '../components/AuthContext';

const mockToken = {
  access_token: 'test-token',
  expires_in: 300,
  refresh_expires_in: 1800,
  refresh_token: 'test-refresh-token',
  scope: 'openid',
  token_type: 'Bearer',
};

function renderLandingPage(opts: { isAuthenticated?: boolean; role?: Role | null } = {}) {
  const { isAuthenticated = false, role = null } = opts;
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated,
    token: isAuthenticated ? mockToken : null,
    username: null,
    role,
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('LandingPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('redirects to /login when not authenticated', () => {
    renderLandingPage({ isAuthenticated: false });

    expect(screen.queryByRole('heading')).not.toBeInTheDocument();
  });

  it('renders AdminLandingPage when role is ADMIN', () => {
    renderLandingPage({ isAuthenticated: true, role: 'ADMIN' });

    expect(screen.getByText('Welcome Admin')).toBeInTheDocument();
    expect(screen.getByText('Department')).toBeInTheDocument();
  });

  it('renders "Welcome Teacher" when role is TEACHER', () => {
    renderLandingPage({ isAuthenticated: true, role: 'TEACHER' });

    expect(screen.getByText('Welcome Teacher')).toBeInTheDocument();
    expect(screen.queryByText('Welcome Admin')).not.toBeInTheDocument();
  });

  it('renders "Welcome Student" when role is STUDENT', () => {
    renderLandingPage({ isAuthenticated: true, role: 'STUDENT' });

    expect(screen.getByText('Welcome Student')).toBeInTheDocument();
  });
});
