import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import AdminLandingPage from './AdminLandingPage';

function renderPage() {
  render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route path='/' element={<AdminLandingPage />} />
        <Route path='/admin/departments' element={<div>Department Page Loaded</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('AdminLandingPage', () => {
  it('renders the welcome heading', () => {
    renderPage();

    expect(screen.getByText('Welcome Admin')).toBeInTheDocument();
  });

  it('renders the Department row with a View link', () => {
    renderPage();

    expect(screen.getByText('Department')).toBeInTheDocument();
    const viewLink = screen.getByRole('link', { name: /view/i });
    expect(viewLink).toHaveAttribute('href', '/admin/departments');
  });

  it('navigates to /admin/departments when View is clicked', async () => {
    renderPage();

    await userEvent.click(screen.getByRole('link', { name: /view/i }));

    expect(await screen.findByText('Department Page Loaded')).toBeInTheDocument();
  });
});
