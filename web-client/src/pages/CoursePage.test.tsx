import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import CoursePage from './CoursePage';
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
        <CoursePage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const mockDepartments = [
  { code: 'CSE', name: 'Computer Science' },
  { code: 'EEE', name: 'Electrical Engineering' },
];

const mockTeachersHal = [
  {
    fullName: 'John Doe',
    _links: { self: { href: 'http://localhost:8100/v1/teachers/7' } },
  },
];

const initialCourses = [
  {
    id: 1,
    code: 'CSE-101',
    name: 'Intro to Java',
    credit: 3,
    description: 'desc',
    departmentCode: 'CSE',
    semester: 'FIRST_YEAR_FIRST',
    instructorId: 7,
  },
  {
    id: 2,
    code: 'CSE-102',
    name: 'Data Structures',
    credit: 3.5,
    description: '',
    departmentCode: 'CSE',
    semester: 'SECOND_YEAR_FIRST',
    instructorId: null,
  },
];

function coursesEnvelope(list: typeof initialCourses) {
  return { content: list, page: 0, size: 5, totalElements: list.length, totalPages: 1 };
}

function mockAllGet(courses = initialCourses) {
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    if (url.includes('/v1/courses')) return { data: coursesEnvelope(courses) };
    if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
    if (url.includes('/v1/teachers')) return { data: { _embedded: { teachers: mockTeachersHal } } };
    return { data: {} };
  });
}

describe('CoursePage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the Courses heading and list with pretty semester labels', async () => {
    mockAllGet();
    renderPage();

    expect(screen.getByText('Courses')).toBeInTheDocument();
    expect(await screen.findByText('Intro to Java')).toBeInTheDocument();
    expect(screen.getByText('Data Structures')).toBeInTheDocument();
    expect(screen.getByText('1st Year - 1st Semester')).toBeInTheDocument();
    expect(screen.getByText('2nd Year - 1st Semester')).toBeInTheDocument();
  });

  it('opens delete confirmation and does not call DELETE on Cancel', async () => {
    mockAllGet();
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    expect(await screen.findByText('Delete course?')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(deleteSpy).not.toHaveBeenCalled();
  });

  it('sends DELETE to the correct URL, refreshes, and shows success snackbar', async () => {
    let coursesCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/courses')) {
        coursesCallCount++;
        const list = coursesCallCount === 1
          ? initialCourses
          : initialCourses.filter((c) => c.id !== 1);
        return { data: coursesEnvelope(list) };
      }
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/teachers')) return { data: { _embedded: { teachers: mockTeachersHal } } };
      return { data: {} };
    });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    await screen.findByText('Delete course?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    await waitFor(() => expect(deleteSpy).toHaveBeenCalledTimes(1));
    expect(deleteSpy).toHaveBeenCalledWith(expect.stringMatching(/\/v1\/courses\/1$/));

    expect(await screen.findByText('Course deleted')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('Intro to Java')).not.toBeInTheDocument());
  });

  it('opens the edit dialog with prefilled values', async () => {
    mockAllGet();
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByLabelText(/code/i)).toHaveValue('CSE-101');
    expect(within(dialog).getByLabelText(/^name/i)).toHaveValue('Intro to Java');
    expect(within(dialog).getByLabelText(/credit/i)).toHaveValue(3);
    expect(within(dialog).getByLabelText(/description/i)).toHaveValue('desc');
  });

  it('sends PUT with correct payload on Update', async () => {
    let coursesCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/courses')) {
        coursesCallCount++;
        const list = coursesCallCount === 1
          ? initialCourses
          : [{ ...initialCourses[0], name: 'Java Updated' }, initialCourses[1]];
        return { data: coursesEnvelope(list) };
      }
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/teachers')) return { data: { _embedded: { teachers: mockTeachersHal } } };
      return { data: {} };
    });
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    const nameInput = within(dialog).getByLabelText(/^name/i);
    await userEvent.clear(nameInput);
    await userEvent.type(nameInput, 'Java Updated');

    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    await waitFor(() => expect(putSpy).toHaveBeenCalledTimes(1));
    const [putUrl, putBody] = putSpy.mock.calls[0];
    expect(putUrl).toMatch(/\/v1\/courses\/1$/);
    expect(putBody).toMatchObject({
      code: 'CSE-101',
      name: 'Java Updated',
      credit: 3,
      description: 'desc',
      departmentCode: 'CSE',
      semester: 'FIRST_YEAR_FIRST',
      instructorId: 7,
    });

    expect(await screen.findByText('Course updated')).toBeInTheDocument();
    expect(await screen.findByText('Java Updated')).toBeInTheDocument();
  });

  it('shows inline validation when required field is cleared', async () => {
    mockAllGet();
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.clear(within(dialog).getByLabelText(/^name/i));
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    expect(await screen.findByText('Name is required')).toBeInTheDocument();
    expect(putSpy).not.toHaveBeenCalled();
  });

  it('shows server errors in an Alert at top of dialog when PUT fails', async () => {
    mockAllGet();
    vi.spyOn(axios, 'put').mockRejectedValue({
      isAxiosError: true,
      response: {
        data: { errors: [{ field: 'code', message: 'must be unique' }] },
      },
    });
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    const alert = await within(dialog).findByRole('alert');
    expect(alert).toHaveTextContent('code: must be unique');
  });

  it('opens an empty add dialog when Add Course is clicked', async () => {
    mockAllGet();
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getByRole('button', { name: /add course/i }));

    expect(await screen.findByText('Add course')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/code/i)).toHaveValue('');
    expect(within(dialog).getByRole('button', { name: /^add$/i })).toBeInTheDocument();
  });

  it('sends POST with payload when adding (instructor None → null)', async () => {
    let coursesCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/courses')) {
        coursesCallCount++;
        const list = coursesCallCount === 1
          ? initialCourses
          : [...initialCourses, {
              id: 99,
              code: 'CSE-201',
              name: 'Algorithms',
              credit: 3,
              description: '',
              departmentCode: 'CSE',
              semester: 'THIRD_YEAR_FIRST',
              instructorId: null,
            }];
        return { data: coursesEnvelope(list) };
      }
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/teachers')) return { data: { _embedded: { teachers: mockTeachersHal } } };
      return { data: {} };
    });
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getByRole('button', { name: /add course/i }));

    const dialog = await screen.findByRole('dialog');
    await userEvent.type(within(dialog).getByLabelText(/code/i), 'CSE-201');
    await userEvent.type(within(dialog).getByLabelText(/^name/i), 'Algorithms');
    const creditInput = within(dialog).getByLabelText(/credit/i);
    await userEvent.clear(creditInput);
    await userEvent.type(creditInput, '3');

    await userEvent.click(within(dialog).getByLabelText(/department/i));
    let listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Computer Science'));

    await userEvent.click(within(dialog).getByLabelText(/semester/i));
    listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('3rd Year - 1st Semester'));

    await userEvent.click(within(dialog).getByRole('button', { name: /^add$/i }));

    await waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const [postUrl, postBody] = postSpy.mock.calls[0];
    expect(postUrl).toMatch(/\/v1\/courses$/);
    expect(postBody).toMatchObject({
      code: 'CSE-201',
      name: 'Algorithms',
      credit: 3,
      departmentCode: 'CSE',
      semester: 'THIRD_YEAR_FIRST',
      instructorId: null,
    });

    expect(await screen.findByText('Course added')).toBeInTheDocument();
    expect(await screen.findByText('Algorithms')).toBeInTheDocument();
  });

  it('filters the table by Semester server-side', async () => {
    const getSpy = vi.spyOn(axios, 'get').mockImplementation(
      async (url: string, config?: { params?: Record<string, unknown> }) => {
        if (url.includes('/v1/courses')) {
          const list = config?.params?.semester === 'SECOND_YEAR_FIRST'
            ? initialCourses.filter((c) => c.semester === 'SECOND_YEAR_FIRST')
            : initialCourses;
          return { data: coursesEnvelope(list) };
        }
        if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
        if (url.includes('/v1/teachers')) return { data: { _embedded: { teachers: mockTeachersHal } } };
        return { data: {} };
      },
    );
    renderPage();

    expect(await screen.findByText('Intro to Java')).toBeInTheDocument();
    expect(screen.getByText('Data Structures')).toBeInTheDocument();

    const filterStack = screen.getByText('Courses').parentElement!;
    const semesterFilter = within(filterStack).getAllByLabelText(/semester/i)[0];
    await userEvent.click(semesterFilter);
    const listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('2nd Year - 1st Semester'));

    await waitFor(() => expect(screen.queryByText('Intro to Java')).not.toBeInTheDocument());
    expect(screen.getByText('Data Structures')).toBeInTheDocument();

    const coursesCalls = getSpy.mock.calls.filter((c) => String(c[0]).endsWith('/v1/courses'));
    expect(coursesCalls.at(-1)![1]).toEqual({ params: { page: 0, size: 10, semester: 'SECOND_YEAR_FIRST' } });
  });

  it('shows error snackbar when DELETE fails', async () => {
    mockAllGet();
    vi.spyOn(axios, 'delete').mockRejectedValue(new Error('boom'));
    renderPage();

    await screen.findByText('Intro to Java');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    await screen.findByText('Delete course?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    expect(await screen.findByText('Failed to delete course')).toBeInTheDocument();
  });
});
