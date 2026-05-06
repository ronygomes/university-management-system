import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ProtectedPage from './ProtectedPage';
import * as AuthContext from './AuthContext';

function renderProtectedPage(isAuthenticated = false) {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated,
    token: null,
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  render(
    <MemoryRouter>
      <ProtectedPage>
        <p>Protected Content</p>
      </ProtectedPage>
    </MemoryRouter>,
  );
}

describe('ProtectedPage', () => {
  it('redirects to /login when not authenticated', () => {
    renderProtectedPage(false);

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('renders children when authenticated', () => {
    renderProtectedPage(true);

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
});
