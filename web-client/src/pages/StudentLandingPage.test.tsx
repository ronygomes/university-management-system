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
  { id: 10, code: 'CSE-101', name: 'Intro to Java', departmentCode: 'CSE', credit: 3 },
  { id: 11, code: 'CSE-201', name: 'Algorithms', departmentCode: 'CSE', credit: 3 },
  { id: 12, code: 'EEE-101', name: 'Circuits', departmentCode: 'EEE', credit: 3 },
  { id: 13, code: 'CSE-301', name: 'No Schedule', departmentCode: 'CSE', credit: 3 },
];

const schedules = [
  { id: 100, courseId: 10, enrollmentOpen: true },
  { id: 101, courseId: 11, enrollmentOpen: true },
  { id: 102, courseId: 12, enrollmentOpen: true },
  // course 13 intentionally has no schedule
];

const enrollments = [
  { id: 500, studentId: 1, courseScheduleId: 100, enrollmentDate: '2026-01-01T00:00:00Z', status: 'ON_GOING', grade: null },
];

function mockAllGet(opts: { enrollments?: typeof enrollments; selfEmail?: string } = {}) {
  const enrols = opts.enrollments ?? enrollments;
  const selfEmail = opts.selfEmail ?? 'jane@ums.dev';
  const self = students.find((s) => s.email === selfEmail);
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    // Specific endpoints first since /v1/me/enrollments contains /v1/enrollments
    if (url.includes('/v1/me/student')) {
      if (!self) throw { isAxiosError: true, response: { status: 403 } };
      return { data: self };
    }
    if (url.includes('/v1/me/enrollments')) {
      return { data: self ? enrols.filter((e) => e.studentId === self.id) : [] };
    }
    if (url.includes('/v1/schedules')) return { data: { content: schedules } };
    if (url.includes('/v1/courses')) return { data: { content: courses } };
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

    // Now appears in both the new Academic Result table and the Enrolled Courses table
    expect((await screen.findAllByText('CSE-101')).length).toBeGreaterThan(0);
    expect(screen.getAllByText('Intro to Java').length).toBeGreaterThan(0);
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

  it('excludes courses whose schedule has enrollmentOpen=false', async () => {
    const closedSchedules = [
      { id: 100, courseId: 10, enrollmentOpen: true },
      // CSE-201 now closed
      { id: 101, courseId: 11, enrollmentOpen: false },
      { id: 102, courseId: 12, enrollmentOpen: true },
    ];
    const jane = students.find((s) => s.email === 'jane@ums.dev');
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/me/student')) return { data: jane };
      if (url.includes('/v1/me/enrollments')) return { data: enrollments.filter((e) => e.studentId === jane!.id) };
      if (url.includes('/v1/schedules')) return { data: { content: closedSchedules } };
      if (url.includes('/v1/courses')) return { data: { content: courses } };
      return { data: {} };
    });
    renderPage('jane@ums.dev');

    await screen.findByText('Welcome, Jane Doe');
    await userEvent.click(screen.getByLabelText(/course to enroll/i));
    const listbox = await screen.findByRole('listbox');
    // CSE-201 should NOT appear (its schedule has enrollmentOpen=false)
    expect(within(listbox).queryByText(/CSE-201/)).not.toBeInTheDocument();
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

  it('computes CGPA and shows certificate eligibility when all enrollments are PASSED', async () => {
    const enrolPassed = [
      { id: 700, studentId: 1, courseScheduleId: 100, enrollmentDate: '2026-01-01T00:00:00Z', status: 'PASSED', grade: 'A_PLUS' },
      { id: 701, studentId: 1, courseScheduleId: 101, enrollmentDate: '2026-01-01T00:00:00Z', status: 'PASSED', grade: 'B' },
    ];
    mockAllGet({ enrollments: enrolPassed as unknown as typeof enrollments });
    renderPage('jane@ums.dev');

    await screen.findByText('Welcome, Jane Doe');

    // CGPA = (4.00*3 + 3.00*3) / 6 = 3.50
    expect(await screen.findByText('3.50')).toBeInTheDocument();
    expect(screen.getByText('You are eligible to get the certificate.')).toBeInTheDocument();
  });

  it('shows CGPA as N/A and no eligibility while courses are ON_GOING', async () => {
    mockAllGet();
    renderPage('jane@ums.dev');

    await screen.findByText('Welcome, Jane Doe');

    expect(await screen.findByText('N/A')).toBeInTheDocument();
    expect(screen.queryByText('You are eligible to get the certificate.')).not.toBeInTheDocument();
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

