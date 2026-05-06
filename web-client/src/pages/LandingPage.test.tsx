import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import axios from 'axios';
import LandingPage from './LandingPage';
import * as AuthContext from '../components/AuthContext';

const mockToken = {
  access_token: 'test-token',
  expires_in: 300,
  refresh_expires_in: 1800,
  refresh_token: 'test-refresh-token',
  scope: 'openid',
  token_type: 'Bearer',
};

function renderLandingPage(isAuthenticated = false) {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated,
    token: isAuthenticated ? mockToken : null,
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  render(
    <MemoryRouter>
      <LandingPage />
    </MemoryRouter>,
  );
}

describe('LandingPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('redirects to /login when not authenticated', () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    renderLandingPage(false);

    expect(screen.queryByText('Welcome Authenticated User')).not.toBeInTheDocument();
  });

  it('renders the welcome heading when authenticated', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    renderLandingPage(true);

    expect(await screen.findByText('Welcome Authenticated User')).toBeInTheDocument();
  });

  it('renders department names fetched from the API', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({
      data: {
        _embedded: {
          departments: [
            { code: 'CSE', name: 'Computer Science' },
            { code: 'EEE', name: 'Electrical Engineering' },
          ],
        },
      },
    });
    renderLandingPage(true);

    expect(await screen.findByText('Computer Science')).toBeInTheDocument();
    expect(screen.getByText('Electrical Engineering')).toBeInTheDocument();
  });
});
