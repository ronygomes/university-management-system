import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from './LoginPage';
import * as AuthContext from '../components/AuthContext';

const mockLoginHandler = vi.fn();

function renderLoginPage(isAuthenticated = false) {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated,
    token: null,
    loginHandler: mockLoginHandler,
    logoutHandler: vi.fn(),
  });

  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    mockLoginHandler.mockReset();
  });

  it('renders the email field, password field, and sign in button', () => {
    renderLoginPage();

    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('redirects to / when already authenticated', () => {
    renderLoginPage(true);

    expect(screen.queryByPlaceholderText('Email')).not.toBeInTheDocument();
  });

  it('calls loginHandler with typed email and password on submit', async () => {
    mockLoginHandler.mockResolvedValue(true);
    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Email'), 'admin@ums.com');
    await userEvent.type(screen.getByPlaceholderText('Password'), 'secret');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(mockLoginHandler).toHaveBeenCalledWith({
      email: 'admin@ums.com',
      password: 'secret',
    });
  });

  it('shows error snackbar when login fails', async () => {
    mockLoginHandler.mockResolvedValue(false);
    renderLoginPage();

    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Invalid email or password')).toBeInTheDocument();
  });

  it('does not show error snackbar when login succeeds', async () => {
    mockLoginHandler.mockResolvedValue(true);
    renderLoginPage();

    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(screen.queryByText('Invalid email or password')).not.toBeInTheDocument();
  });
});
