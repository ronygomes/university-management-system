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
    username: null,
    email: null,
    role: null,
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

  it('renders the username field, password field, and sign in button', () => {
    renderLoginPage();

    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('redirects to / when already authenticated', () => {
    renderLoginPage(true);

    expect(screen.queryByPlaceholderText('Username')).not.toBeInTheDocument();
  });

  it('calls loginHandler with typed username and password on submit', async () => {
    mockLoginHandler.mockResolvedValue(true);
    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Username'), 'admin');
    await userEvent.type(screen.getByPlaceholderText('Password'), 'secret');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(mockLoginHandler).toHaveBeenCalledWith(
      { username: 'admin', password: 'secret' },
      true,
    );
  });

  it('passes remember=false when the Remember me checkbox is unchecked', async () => {
    mockLoginHandler.mockResolvedValue(true);
    renderLoginPage();

    await userEvent.type(screen.getByPlaceholderText('Username'), 'admin');
    await userEvent.type(screen.getByPlaceholderText('Password'), 'secret');
    await userEvent.click(screen.getByRole('checkbox', { name: /remember me/i }));
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(mockLoginHandler).toHaveBeenCalledWith(
      { username: 'admin', password: 'secret' },
      false,
    );
  });

  it('shows error snackbar when login fails', async () => {
    mockLoginHandler.mockResolvedValue(false);
    renderLoginPage();

    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Invalid username or password')).toBeInTheDocument();
  });

  it('does not show error snackbar when login succeeds', async () => {
    mockLoginHandler.mockResolvedValue(true);
    renderLoginPage();

    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(screen.queryByText('Invalid username or password')).not.toBeInTheDocument();
  });
});
