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
        <Route path='/admin/teachers' element={<div>Teacher Page Loaded</div>} />
        <Route path='/admin/courses' element={<div>Course Page Loaded</div>} />
        <Route path='/admin/schedules' element={<div>Schedule Page Loaded</div>} />
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

  it('renders the Teacher row with a View link to /admin/teachers', () => {
    renderPage();

    expect(screen.getByText('Teacher')).toBeInTheDocument();
    const teacherRow = screen.getByText('Teacher').closest('div') as HTMLElement;
    const teacherView = within(teacherRow).getByRole('link', { name: /view/i });
    expect(teacherView).toHaveAttribute('href', '/admin/teachers');
  });

  it('renders the Course row with a View link to /admin/courses', () => {
    renderPage();

    expect(screen.getByText('Course')).toBeInTheDocument();
    const courseRow = screen.getByText('Course').closest('div') as HTMLElement;
    const courseView = within(courseRow).getByRole('link', { name: /view/i });
    expect(courseView).toHaveAttribute('href', '/admin/courses');
  });

  it('renders the Schedule row with a View link to /admin/schedules', () => {
    renderPage();

    expect(screen.getByText('Schedule')).toBeInTheDocument();
    const scheduleRow = screen.getByText('Schedule').closest('div') as HTMLElement;
    const scheduleView = within(scheduleRow).getByRole('link', { name: /view/i });
    expect(scheduleView).toHaveAttribute('href', '/admin/schedules');
  });
});
