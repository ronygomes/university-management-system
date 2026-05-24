import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import TeacherLandingPage from './TeacherLandingPage';
import * as AuthContext from '../components/AuthContext';

function renderPage(email: string | null = 'jdoe@ums.dev') {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated: true,
    token: null,
    username: 'jdoe',
    email,
    role: 'TEACHER',
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <TeacherLandingPage />
    </QueryClientProvider>,
  );
}

const teachersByEmail: Record<string, { id: number; fullName: string; email: string }> = {
  'jdoe@ums.dev': { id: 7, fullName: 'John Doe', email: 'jdoe@ums.dev' },
  'jane@ums.dev': { id: 8, fullName: 'Jane Smith', email: 'jane@ums.dev' },
};

const courses = [
  { id: 1, title: 'CSE-101', name: 'Intro to Java', credit: 3, semester: 'FIRST_YEAR_FIRST', departmentCode: 'CSE', description: '', instructorId: 7 },
  { id: 2, title: 'CSE-201', name: 'Algorithms', credit: 3, semester: 'SECOND_YEAR_FIRST', departmentCode: 'CSE', description: '', instructorId: 7 },
  { id: 3, title: 'CSE-301', name: 'Compilers', credit: 3, semester: 'THIRD_YEAR_FIRST', departmentCode: 'CSE', description: '', instructorId: 8 },
];

const schedules = [
  { id: 10, courseId: 1, building: 'BUILDING_1', roomNumber: 'F7-102', days: ['MONDAY', 'WEDNESDAY'], startDate: '2026-01-15T09:00:00.000Z', endDate: '2026-01-15T10:30:00.000Z' },
  { id: 11, courseId: 3, building: 'BUILDING_2', roomNumber: 'F8-201', days: ['TUESDAY'], startDate: '2026-01-16T11:00:00.000Z', endDate: '2026-01-16T12:30:00.000Z' },
];

function mockAllGet(selfEmail: string | null = 'jdoe@ums.dev') {
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    if (url.includes('/v1/me/teacher')) {
      const self = selfEmail ? teachersByEmail[selfEmail] : undefined;
      if (!self) throw { isAxiosError: true, response: { status: 403 } };
      return { data: self };
    }
    if (url.includes('/v1/schedules')) return { data: schedules };
    if (url.includes('/v1/courses')) return { data: courses };
    return { data: {} };
  });
}

describe('TeacherLandingPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('shows only courses where instructorId matches the current teacher', async () => {
    mockAllGet();
    renderPage('jdoe@ums.dev');

    expect(await screen.findByText('Welcome, John Doe')).toBeInTheDocument();
    // CSE-101 appears in both course list and schedule list
    expect(screen.getAllByText('CSE-101').length).toBeGreaterThan(0);
    expect(screen.getByText('CSE-201')).toBeInTheDocument();
    // CSE-301 has instructorId 8 (Jane Smith), not the logged-in teacher
    expect(screen.queryByText('CSE-301')).not.toBeInTheDocument();
  });

  it('shows only schedules whose course belongs to the current teacher', async () => {
    mockAllGet();
    renderPage('jdoe@ums.dev');

    expect(await screen.findByText('F7-102')).toBeInTheDocument();
    // schedule for CSE-301 (room F8-201) belongs to Jane Smith — should not appear
    expect(screen.queryByText('F8-201')).not.toBeInTheDocument();
  });

  it('shows the fallback welcome and empty-state rows when the teacher record is not found', async () => {
    // Backend returns 403 (UmsDataException → ENTITY_NOT_FOUND maps to FORBIDDEN);
    // page falls back to "Welcome Teacher" + empty tables.
    mockAllGet('not-a-teacher@ums.dev');
    renderPage('not-a-teacher@ums.dev');

    expect(await screen.findByText('Welcome Teacher')).toBeInTheDocument();
    expect(screen.getByText('No assigned courses yet.')).toBeInTheDocument();
    expect(screen.getByText('No schedule yet.')).toBeInTheDocument();
  });
});
