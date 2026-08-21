import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import CourseSchedulePage from './CourseSchedulePage';
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
        <CourseSchedulePage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const mockDepartments = [
  { code: 'CSE', name: 'Computer Science' },
  { code: 'EEE', name: 'Electrical Engineering' },
];

const mockCourses = [
  { id: 1, code: 'CSE-101', name: 'Intro to Java', departmentCode: 'CSE', semester: 'FIRST_YEAR_FIRST', credit: 3, description: '', instructorId: null },
  { id: 2, code: 'CSE-102', name: 'Data Structures', departmentCode: 'CSE', semester: 'FIRST_YEAR_FIRST', credit: 3, description: '', instructorId: null },
  { id: 3, code: 'EEE-101', name: 'Circuits', departmentCode: 'EEE', semester: 'FIRST_YEAR_FIRST', credit: 3, description: '', instructorId: null },
];

const initialSchedules = [
  {
    id: 10,
    courseId: 1,
    department: { code: 'CSE', name: 'Computer Science' },
    semester: 'FIRST_YEAR_FIRST',
    building: 'BUILDING_1',
    roomNumber: 'F7-102',
    days: ['MONDAY', 'TUESDAY'],
    startDate: '2026-01-15T09:00:00.000Z',
    endDate: '2026-01-15T10:30:00.000Z',
    enrollmentOpen: true,
  },
];

function mockAllGet(schedules = initialSchedules) {
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    if (url.includes('/v1/schedules')) return { data: { content: schedules } };
    if (url.includes('/v1/courses')) return { data: { content: mockCourses } };
    if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
    return { data: {} };
  });
}

describe('CourseSchedulePage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the heading, table, and a schedule row with course title', async () => {
    mockAllGet();
    renderPage();

    expect(screen.getByText('Schedules')).toBeInTheDocument();
    expect(await screen.findByText('CSE-101')).toBeInTheDocument();
    expect(screen.getByText('1st Year - 1st Semester')).toBeInTheDocument();
    expect(screen.getByText('Building 1')).toBeInTheDocument();
    expect(screen.getByText('F7-102')).toBeInTheDocument();
    expect(screen.getByText('Mon, Tue')).toBeInTheDocument();
  });

  it('opens delete confirmation and does not call DELETE on Cancel', async () => {
    mockAllGet();
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    expect(await screen.findByText('Delete schedule?')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(deleteSpy).not.toHaveBeenCalled();
  });

  it('sends DELETE to the correct URL and shows success snackbar', async () => {
    let schedulesCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/schedules')) {
        schedulesCallCount++;
        return { data: { content: schedulesCallCount === 1 ? initialSchedules : [] } };
      }
      if (url.includes('/v1/courses')) return { data: { content: mockCourses } };
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      return { data: {} };
    });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    await screen.findByText('Delete schedule?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    await waitFor(() => expect(deleteSpy).toHaveBeenCalledTimes(1));
    expect(deleteSpy).toHaveBeenCalledWith(expect.stringMatching(/\/v1\/schedules\/10$/));

    expect(await screen.findByText('Schedule deleted')).toBeInTheDocument();
  });

  it('opens the edit dialog with prefilled values', async () => {
    mockAllGet();
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    expect(await screen.findByText('Edit schedule')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/room number/i)).toHaveValue('F7-102');
  });

  it('shows inline validation when Room Number is cleared', async () => {
    mockAllGet();
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.clear(within(dialog).getByLabelText(/room number/i));
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    expect(await screen.findByText('Room number is required')).toBeInTheDocument();
    expect(putSpy).not.toHaveBeenCalled();
  });

  it('sends POST with payload when adding a new schedule', async () => {
    let schedulesCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/schedules')) {
        schedulesCallCount++;
        return { data: { content: schedulesCallCount === 1 ? initialSchedules : [
          ...initialSchedules,
          {
            id: 11,
            courseId: 2,
            department: { code: 'CSE', name: 'Computer Science' },
            semester: 'FIRST_YEAR_FIRST',
            building: 'BUILDING_2',
            roomNumber: 'F8-201',
            days: ['WEDNESDAY'],
            startDate: '2026-02-01T11:00:00.000Z',
            endDate: '2026-02-01T12:30:00.000Z',
            enrollmentOpen: true,
          },
        ] } };
      }
      if (url.includes('/v1/courses')) return { data: { content: mockCourses } };
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      return { data: {} };
    });
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getByRole('button', { name: /add schedule/i }));

    const dialog = await screen.findByRole('dialog');

    await userEvent.click(within(dialog).getByLabelText(/department/i));
    let listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Computer Science'));

    await userEvent.click(within(dialog).getByLabelText(/semester/i));
    listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('1st Year - 1st Semester'));

    await userEvent.click(within(dialog).getByLabelText(/^course\b/i));
    listbox = await screen.findByRole('listbox');
    // CSE-101 is already scheduled (id 1) — should NOT be in the listbox; CSE-102 should be
    expect(within(listbox).queryByText(/CSE-101/)).not.toBeInTheDocument();
    await userEvent.click(within(listbox).getByText(/CSE-102/));

    await userEvent.click(within(dialog).getByLabelText(/building/i));
    listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Building 2'));

    await userEvent.type(within(dialog).getByLabelText(/room number/i), 'F8-201');

    await userEvent.click(within(dialog).getByLabelText(/days/i));
    listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Wednesday'));
    await userEvent.keyboard('{Escape}');

    await userEvent.type(within(dialog).getByLabelText(/start/i), '2026-02-01T11:00');
    await userEvent.type(within(dialog).getByLabelText(/end/i), '2026-02-01T12:30');

    await userEvent.click(within(dialog).getByRole('button', { name: /^add$/i }));

    await waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const [postUrl, postBody] = postSpy.mock.calls[0];
    expect(postUrl).toMatch(/\/v1\/schedules$/);
    expect(postBody).toMatchObject({
      departmentCode: 'CSE',
      semester: 'FIRST_YEAR_FIRST',
      courseId: 2,
      building: 'BUILDING_2',
      roomNumber: 'F8-201',
      days: ['WEDNESDAY'],
    });

    expect(await screen.findByText('Schedule added')).toBeInTheDocument();
  });

  it('shows server errors in an Alert at top of dialog when PUT fails', async () => {
    mockAllGet();
    vi.spyOn(axios, 'put').mockRejectedValue({
      isAxiosError: true,
      response: { data: { errors: [{ field: 'days', message: 'must not be empty' }] } },
    });
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    const alert = await within(dialog).findByRole('alert');
    expect(alert).toHaveTextContent('days: must not be empty');
  });

  it('toggles enrollmentOpen via PUT to /enrollment-open with ?open=', async () => {
    mockAllGet();
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    // initialSchedules[0].enrollmentOpen === true, button shows "Open . Close"
    const toggle = await screen.findByRole('button', { name: /open · close/i });
    await userEvent.click(toggle);

    await waitFor(() => expect(putSpy).toHaveBeenCalledTimes(1));
    expect(putSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/schedules\/10\/enrollment-open$/),
      null,
      { params: { open: false } },
    );
    expect(await screen.findByText('Enrollment closed')).toBeInTheDocument();
  });

  it('shows error snackbar when DELETE fails', async () => {
    mockAllGet();
    vi.spyOn(axios, 'delete').mockRejectedValue(new Error('boom'));
    renderPage();

    await screen.findByText('CSE-101');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    await screen.findByText('Delete schedule?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    expect(await screen.findByText('Failed to delete schedule')).toBeInTheDocument();
  });
});
