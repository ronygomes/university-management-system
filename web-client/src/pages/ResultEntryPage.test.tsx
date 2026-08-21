import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import ResultEntryPage from './ResultEntryPage';
import * as AuthContext from '../components/AuthContext';

function renderPage() {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated: true,
    token: null,
    username: null,
    email: null,
    role: 'ADMIN',
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ResultEntryPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const departments = [
  { code: 'CSE', name: 'Computer Science' },
  { code: 'EEE', name: 'Electrical Engineering' },
];
const courses = [
  { id: 10, code: 'CSE-101', name: 'Intro to Java', departmentCode: 'CSE' },
  { id: 11, code: 'CSE-201', name: 'Algorithms', departmentCode: 'CSE' },
];
const schedules = [
  { id: 100, courseId: 10 },
  { id: 101, courseId: 11 },
];
const students = [
  { id: 1, fullName: 'Jane Doe', email: 'jane@u.edu', registrationNumber: '202500CSE0001' },
  { id: 2, fullName: 'Bob Brown', email: 'bob@u.edu', registrationNumber: '202500CSE0002' },
];
const enrollments = [
  { id: 500, studentId: 1, courseScheduleId: 100, enrollmentDate: '2026-01-01T00:00:00Z', status: 'ON_GOING', grade: null },
  { id: 501, studentId: 2, courseScheduleId: 100, enrollmentDate: '2026-01-01T00:00:00Z', status: 'ON_GOING', grade: null },
  { id: 502, studentId: 1, courseScheduleId: 101, enrollmentDate: '2026-01-01T00:00:00Z', status: 'ON_GOING', grade: null },
];

function mockAllGet() {
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    if (url.includes('/v1/departments')) return { data: { _embedded: { departments } } };
    if (url.includes('/v1/students')) return { data: { content: students } };
    if (url.includes('/v1/schedules')) return { data: { content: schedules } };
    if (url.includes('/v1/courses')) return { data: { content: courses } };
    if (url.includes('/v1/enrollments')) return { data: { content: enrollments } };
    return { data: {} };
  });
}

describe('ResultEntryPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('shows the heading and empty-state row before any filter is set', async () => {
    mockAllGet();
    renderPage();

    expect(screen.getByText('Result Entry')).toBeInTheDocument();
    expect(await screen.findByText(/Select a department and course/)).toBeInTheDocument();
  });

  it('loads roster after Department + Course are selected', async () => {
    mockAllGet();
    renderPage();

    await userEvent.click(screen.getByLabelText(/department/i));
    let listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Computer Science'));

    await userEvent.click(screen.getByLabelText(/^course/i));
    listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText(/CSE-101/));

    expect(await screen.findByText('Jane Doe')).toBeInTheDocument();
    expect(screen.getByText('Bob Brown')).toBeInTheDocument();
    // Enrollment 502 is for CSE-201, should NOT appear
    expect(screen.queryByText('202500EEE0001')).not.toBeInTheDocument();
  });

  it('PATCHes with grade and status=PASSED for non-F grade', async () => {
    mockAllGet();
    const patchSpy = vi.spyOn(axios, 'patch').mockResolvedValue({});
    renderPage();

    await userEvent.click(screen.getByLabelText(/department/i));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText('Computer Science'));
    await userEvent.click(screen.getByLabelText(/^course/i));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText(/CSE-101/));

    const janeRow = (await screen.findByText('Jane Doe')).closest('tr') as HTMLElement;
    await userEvent.click(within(janeRow).getByRole('combobox'));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText('A+'));

    await userEvent.click(within(janeRow).getByRole('button', { name: /save/i }));

    await waitFor(() => expect(patchSpy).toHaveBeenCalledTimes(1));
    expect(patchSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/enrollments\/500$/),
      { grade: 'A_PLUS', status: 'PASSED' },
    );

    expect(await screen.findByText('Grade saved')).toBeInTheDocument();
  });

  it('PATCHes with status=FAILED when grade is F', async () => {
    mockAllGet();
    const patchSpy = vi.spyOn(axios, 'patch').mockResolvedValue({});
    renderPage();

    await userEvent.click(screen.getByLabelText(/department/i));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText('Computer Science'));
    await userEvent.click(screen.getByLabelText(/^course/i));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText(/CSE-101/));

    const bobRow = (await screen.findByText('Bob Brown')).closest('tr') as HTMLElement;
    await userEvent.click(within(bobRow).getByRole('combobox'));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText('F'));
    await userEvent.click(within(bobRow).getByRole('button', { name: /save/i }));

    await waitFor(() => expect(patchSpy).toHaveBeenCalledTimes(1));
    expect(patchSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/enrollments\/501$/),
      { grade: 'F', status: 'FAILED' },
    );
  });

  it('Save button is disabled until a grade is selected', async () => {
    mockAllGet();
    renderPage();

    await userEvent.click(screen.getByLabelText(/department/i));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText('Computer Science'));
    await userEvent.click(screen.getByLabelText(/^course/i));
    await userEvent.click(within(await screen.findByRole('listbox')).getByText(/CSE-101/));

    const janeRow = (await screen.findByText('Jane Doe')).closest('tr') as HTMLElement;
    expect(within(janeRow).getByRole('button', { name: /save/i })).toBeDisabled();
  });
});
