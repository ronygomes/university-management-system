import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import ContentWrapper from './ContentWrapper';
import * as AuthContext from './AuthContext';
import type { Role } from './AuthContext';

const mockLogoutHandler = vi.fn();

function renderWrapper(opts: { username?: string | null; role?: Role | null } = {}) {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated: true,
    token: null,
    username: opts.username ?? 'jdoe',
    role: opts.role ?? 'TEACHER',
    loginHandler: vi.fn(),
    logoutHandler: mockLogoutHandler,
  });

  render(
    <MemoryRouter>
      <ContentWrapper><p>Test Child</p></ContentWrapper>
    </MemoryRouter>,
  );
}

describe('ContentWrapper', () => {
  beforeEach(() => {
    mockLogoutHandler.mockReset();
  });

  it('renders the app bar with the system title', () => {
    renderWrapper();

    expect(screen.getByText('University Management System')).toBeInTheDocument();
  });

  it('renders children', () => {
    renderWrapper();

    expect(screen.getByText('Test Child')).toBeInTheDocument();
  });

  it('renders username and role from auth context', () => {
    renderWrapper({ username: 'jdoe', role: 'TEACHER' });

    expect(screen.getByText('jdoe (Teacher)')).toBeInTheDocument();
  });

  it('formats Admin and Student roles', () => {
    renderWrapper({ username: 'alice', role: 'ADMIN' });
    expect(screen.getByText('alice (Admin)')).toBeInTheDocument();
  });

  it('calls logoutHandler when the logout button is clicked', async () => {
    renderWrapper();

    await userEvent.click(screen.getByRole('button', { name: /logout/i }));

    expect(mockLogoutHandler).toHaveBeenCalledTimes(1);
  });
});
