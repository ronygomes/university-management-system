import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import AdminLandingPage from './AdminLandingPage';

function renderPage() {
  render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route path='/' element={<AdminLandingPage />} />
        <Route path='/admin/departments' element={<div>Department Page Loaded</div>} />
        <Route path='/admin/designations' element={<div>Designation Page Loaded</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('AdminLandingPage', () => {
  it('renders the welcome heading', () => {
    renderPage();

    expect(screen.getByText('Welcome Admin')).toBeInTheDocument();
  });

  it('renders the Department row with a View link to /admin/departments', () => {
    renderPage();

    expect(screen.getByText('Department')).toBeInTheDocument();
    const departmentRow = screen.getByText('Department').closest('div') as HTMLElement;
    const departmentView = within(departmentRow).getByRole('link', { name: /view/i });
    expect(departmentView).toHaveAttribute('href', '/admin/departments');
  });

  it('navigates to /admin/departments when its View is clicked', async () => {
    renderPage();

    const departmentRow = screen.getByText('Department').closest('div') as HTMLElement;
    const departmentView = within(departmentRow).getByRole('link', { name: /view/i });
    await userEvent.click(departmentView);

    expect(await screen.findByText('Department Page Loaded')).toBeInTheDocument();
  });

  it('renders the Designation row with a View link to /admin/designations', () => {
    renderPage();

    expect(screen.getByText('Designation')).toBeInTheDocument();
    const designationRow = screen.getByText('Designation').closest('div') as HTMLElement;
    const designationView = within(designationRow).getByRole('link', { name: /view/i });
    expect(designationView).toHaveAttribute('href', '/admin/designations');
  });
});
