import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import StudentLandingPage from './StudentLandingPage';
import * as AuthContext from '../components/AuthContext';

function renderPage(email: string | null = 'jane@ums.dev') {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated: true,
    token: null,
    username: 'jane',
    email,
    role: 'STUDENT',
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <StudentLandingPage />
    </QueryClientProvider>,
  );
}

const students = [
  { id: 1, fullName: 'Jane Doe', email: 'jane@ums.dev', registrationNumber: '202500CSE0001', departmentCode: 'CSE' },
  { id: 2, fullName: 'Bob Brown', email: 'bob@ums.dev', registrationNumber: '202500EEE0001', departmentCode: 'EEE' },
];

const courses = [
  { id: 10, title: 'CSE-101', name: 'Intro to Java', departmentCode: 'CSE' },
  { id: 11, title: 'CSE-201', name: 'Algorithms', departmentCode: 'CSE' },
  { id: 12, title: 'EEE-101', name: 'Circuits', departmentCode: 'EEE' },
  { id: 13, title: 'CSE-301', name: 'No Schedule', departmentCode: 'CSE' },
];

const schedules = [
  { id: 100, courseId: 10 },
  { id: 101, courseId: 11 },
  { id: 102, courseId: 12 },
  // course 13 intentionally has no schedule
];

const enrollments = [
  { id: 500, studentId: 1, courseScheduleId: 100, enrollmentDate: '2026-01-01T00:00:00Z', status: 'ON_GOING' },
];

function mockAllGet(opts: { enrollments?: typeof enrollments } = {}) {
  const enrols = opts.enrollments ?? enrollments;
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    if (url.includes('/v1/students')) return { data: students };
    if (url.includes('/v1/schedules')) return { data: schedules };
    if (url.includes('/v1/courses')) return { data: courses };
    if (url.includes('/v1/enrollments')) return { data: enrols };
    return { data: {} };
  });
}

describe('StudentLandingPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('shows Welcome with student name and Reg No when found', async () => {
    mockAllGet();
    renderPage('jane@ums.dev');

    expect(await screen.findByText('Welcome, Jane Doe')).toBeInTheDocument();
    expect(screen.getByText('202500CSE0001')).toBeInTheDocument();
  });

  it('shows fallback when JWT email does not match a student', async () => {
    mockAllGet();
    renderPage('ghost@ums.dev');

    expect(await screen.findByText('Welcome Student')).toBeInTheDocument();
    expect(screen.getByText(/Student record not found/)).toBeInTheDocument();
  });

  it('shows enrolled courses in the list', async () => {
    mockAllGet();
    renderPage('jane@ums.dev');

    expect(await screen.findByText('CSE-101')).toBeInTheDocument();
    expect(screen.getByText('Intro to Java')).toBeInTheDocument();
  });

  it('Course dropdown excludes already-enrolled and courses without a schedule, and is restricted to own dept by default', async () => {
    mockAllGet();
    renderPage('jane@ums.dev');

    await screen.findByText('Welcome, Jane Doe');

    await userEvent.click(screen.getByLabelText(/course to enroll/i));
    const listbox = await screen.findByRole('listbox');

    // CSE-201 is the only CSE course not yet enrolled and with a schedule
    expect(within(listbox).getByText(/CSE-201/)).toBeInTheDocument();
    // CSE-101 already enrolled
    expect(within(listbox).queryByText(/CSE-101 —/)).not.toBeInTheDocument();
    // CSE-301 has no schedule
    expect(within(listbox).queryByText(/CSE-301 —/)).not.toBeInTheDocument();
    // EEE-101 is not in Jane's department
    expect(within(listbox).queryByText(/EEE-101 —/)).not.toBeInTheDocument();
  });

  it('toggling "Show all" reveals other-department courses', async () => {
    mockAllGet();
    renderPage('jane@ums.dev');

    await screen.findByText('Welcome, Jane Doe');
    await userEvent.click(screen.getByRole('switch', { name: /show all courses/i }));

    await userEvent.click(screen.getByLabelText(/course to enroll/i));
    const listbox = await screen.findByRole('listbox');
    expect(within(listbox).getByText(/EEE-101/)).toBeInTheDocument();
  });

  it('POSTs an enrollment with the chosen schedule id and current date', async () => {
    mockAllGet();
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage('jane@ums.dev');

    await screen.findByText('Welcome, Jane Doe');

    await userEvent.click(screen.getByLabelText(/course to enroll/i));
    const listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText(/CSE-201/));

    await userEvent.click(screen.getByRole('button', { name: /enroll/i }));

    await waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const [postUrl, postBody] = postSpy.mock.calls[0];
    expect(postUrl).toMatch(/\/v1\/enrollments$/);
    expect(postBody).toMatchObject({
      studentId: 1,
      courseScheduleId: 101,
      status: 'ON_GOING',
    });
    expect(typeof (postBody as { enrollmentDate: string }).enrollmentDate).toBe('string');

    expect(await screen.findByText('Enrolled successfully')).toBeInTheDocument();
  });
});

