import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import TeacherPage from './TeacherPage';
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
        <TeacherPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function teacherHal(id: number, fullName: string, title: string, departmentCode = 'CSE', email = `${id}@u.edu`) {
  return {
    fullName,
    email,
    title,
    departmentCode,
    _links: { self: { href: `http://localhost:8100/v1/teachers/${id}` } },
  };
}

const mockDepartments = [
  { code: 'CSE', name: 'Computer Science' },
  { code: 'EEE', name: 'Electrical Engineering' },
];

const mockDesignations = [
  { title: 'Lecturer', _links: { self: { href: 'http://localhost:8100/v1/designations/1' } } },
  { title: 'Professor', _links: { self: { href: 'http://localhost:8100/v1/designations/2' } } },
];

const initialTeachers = [
  teacherHal(1, 'John Doe', 'Lecturer'),
  teacherHal(2, 'Jane Smith', 'Professor'),
];

function teachersPage(list: ReturnType<typeof teacherHal>[]) {
  return {
    _embedded: { teachers: list },
    page: { size: 5, totalElements: list.length, totalPages: 1, number: 0 },
  };
}

const fullTeacher1 = {
  fullName: 'John Doe',
  email: '1@u.edu',
  title: 'Lecturer',
  departmentCode: 'CSE',
  address: '123 Street',
  contactNumber: '+8801712345678',
  assignedCredit: 6,
};

function mockGetByUrl(map: Record<string, unknown>) {
  return vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
    for (const key of Object.keys(map)) {
      if (url.includes(key)) return { data: map[key] };
    }
    return { data: {} };
  });
}

describe('TeacherPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the Teachers heading and the list', async () => {
    mockGetByUrl({
      '/v1/teachers': teachersPage(initialTeachers),
      '/v1/departments': { _embedded: { departments: mockDepartments } },
      '/v1/designations': { _embedded: { designations: mockDesignations } },
    });
    renderPage();

    expect(screen.getByText('Teachers')).toBeInTheDocument();
    expect(await screen.findByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getAllByText('Lecturer').length).toBeGreaterThan(0);
  });

  it('requests the paged teachers endpoint and loads the next page on navigation', async () => {
    let call = 0;
    const getSpy = vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      call++;
      const list = call === 1
        ? [teacherHal(1, 'John Doe', 'Lecturer')]
        : [teacherHal(9, 'Zed Nine', 'Professor')];
      return { data: { _embedded: { teachers: list }, page: { size: 5, totalElements: 10, totalPages: 2, number: call - 1 } } };
    });
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getByRole('button', { name: /next page/i }));
    expect(await screen.findByText('Zed Nine')).toBeInTheDocument();

    const pagedCalls = getSpy.mock.calls.filter((c) => String(c[0]).includes('/v1/teachers/paged'));
    expect(pagedCalls[0][1]).toEqual({ params: { page: 0, size: 5 } });
    expect(pagedCalls[1][1]).toEqual({ params: { page: 1, size: 5 } });
  });

  it('opens delete confirmation and does not call DELETE on Cancel', async () => {
    mockGetByUrl({
      '/v1/teachers': teachersPage(initialTeachers),
      '/v1/departments': { _embedded: { departments: mockDepartments } },
      '/v1/designations': { _embedded: { designations: mockDesignations } },
    });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    expect(await screen.findByText('Delete teacher?')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(deleteSpy).not.toHaveBeenCalled();
  });

  it('sends DELETE to the correct URL, refreshes table, and shows success snackbar', async () => {
    let teachersCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/teachers')) {
        teachersCallCount++;
        const list = teachersCallCount === 1
          ? initialTeachers
          : initialTeachers.filter((t) => t.fullName !== 'John Doe');
        return { data: teachersPage(list) };
      }
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      return { data: {} };
    });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    await screen.findByText('Delete teacher?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    await waitFor(() => expect(deleteSpy).toHaveBeenCalledTimes(1));
    expect(deleteSpy).toHaveBeenCalledWith(expect.stringMatching(/\/v1\/teachers\/1$/));

    expect(await screen.findByText('Teacher deleted')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('John Doe')).not.toBeInTheDocument());
  });

  it('opens the edit dialog with prefilled values from the full GET', async () => {
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.match(/\/v1\/teachers\/\d+$/)) return { data: fullTeacher1 };
      if (url.includes('/v1/teachers')) return { data: teachersPage(initialTeachers) };
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      return { data: {} };
    });
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await within(dialog).findByLabelText(/full name/i);
    expect(within(dialog).getByLabelText(/full name/i)).toHaveValue('John Doe');
    expect(within(dialog).getByLabelText(/email/i)).toHaveValue('1@u.edu');
    expect(within(dialog).getByLabelText(/contact number/i)).toHaveValue('1712345678');
    expect(within(dialog).getByLabelText(/address/i)).toHaveValue('123 Street');
    expect(within(dialog).getByLabelText(/assigned credit/i)).toHaveValue(6);
  });

  it('sends PUT with combined contact number, refreshes table, shows success snackbar', async () => {
    let teachersCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.match(/\/v1\/teachers\/\d+$/)) return { data: fullTeacher1 };
      if (url.includes('/v1/teachers')) {
        teachersCallCount++;
        const list = teachersCallCount === 1
          ? initialTeachers
          : [teacherHal(1, 'John Doe Updated', 'Lecturer'), initialTeachers[1]];
        return { data: teachersPage(list) };
      }
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      return { data: {} };
    });
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await within(dialog).findByLabelText(/full name/i);

    const fullNameInput = within(dialog).getByLabelText(/full name/i);
    await userEvent.clear(fullNameInput);
    await userEvent.type(fullNameInput, 'John Doe Updated');

    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    await waitFor(() => expect(putSpy).toHaveBeenCalledTimes(1));
    const [putUrl, putBody] = putSpy.mock.calls[0];
    expect(putUrl).toMatch(/\/v1\/teachers\/1$/);
    expect(putBody).toMatchObject({
      fullName: 'John Doe Updated',
      email: '1@u.edu',
      title: 'Lecturer',
      departmentCode: 'CSE',
      address: '123 Street',
      contactNumber: '+8801712345678',
      assignedCredit: 6,
    });

    expect(await screen.findByText('Teacher updated')).toBeInTheDocument();
    expect(await screen.findByText('John Doe Updated')).toBeInTheDocument();
  });

  it('shows inline validation when required field is cleared', async () => {
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.match(/\/v1\/teachers\/\d+$/)) return { data: fullTeacher1 };
      if (url.includes('/v1/teachers')) return { data: teachersPage(initialTeachers) };
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      return { data: {} };
    });
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await within(dialog).findByLabelText(/full name/i);
    await userEvent.clear(within(dialog).getByLabelText(/full name/i));
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    expect(await screen.findByText('Full name is required')).toBeInTheDocument();
    expect(putSpy).not.toHaveBeenCalled();
  });

  it('shows server errors in an Alert at top of dialog when PUT fails', async () => {
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.match(/\/v1\/teachers\/\d+$/)) return { data: fullTeacher1 };
      if (url.includes('/v1/teachers')) return { data: teachersPage(initialTeachers) };
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      return { data: {} };
    });
    vi.spyOn(axios, 'put').mockRejectedValue({
      isAxiosError: true,
      response: {
        data: {
          errors: [{ field: 'email', message: 'Email already in use' }],
        },
      },
    });
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await within(dialog).findByLabelText(/full name/i);
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    const alert = await within(dialog).findByRole('alert');
    expect(alert).toHaveTextContent('email: Email already in use');
  });

  it('opens an empty add dialog when Add Teacher is clicked', async () => {
    mockGetByUrl({
      '/v1/teachers': teachersPage(initialTeachers),
      '/v1/departments': { _embedded: { departments: mockDepartments } },
      '/v1/designations': { _embedded: { designations: mockDesignations } },
    });
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getByRole('button', { name: /add teacher/i }));

    expect(await screen.findByText('Add teacher')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/full name/i)).toHaveValue('');
    expect(within(dialog).getByRole('button', { name: /^add$/i })).toBeInTheDocument();
  });

  it('sends POST when adding, refreshes table, and shows success snackbar', async () => {
    let teachersCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async (url: string) => {
      if (url.includes('/v1/teachers')) {
        teachersCallCount++;
        const list = teachersCallCount === 1
          ? initialTeachers
          : [...initialTeachers, teacherHal(99, 'New Guy', 'Professor')];
        return { data: teachersPage(list) };
      }
      if (url.includes('/v1/departments')) return { data: { _embedded: { departments: mockDepartments } } };
      if (url.includes('/v1/designations')) return { data: { _embedded: { designations: mockDesignations } } };
      return { data: {} };
    });
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getByRole('button', { name: /add teacher/i }));

    const dialog = await screen.findByRole('dialog');
    await userEvent.type(within(dialog).getByLabelText(/full name/i), 'New Guy');
    await userEvent.type(within(dialog).getByLabelText(/email/i), 'new@u.edu');

    await userEvent.click(within(dialog).getByLabelText(/department/i));
    let listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Computer Science'));

    await userEvent.click(within(dialog).getByLabelText(/designation/i));
    listbox = await screen.findByRole('listbox');
    await userEvent.click(within(listbox).getByText('Professor'));

    await userEvent.type(within(dialog).getByLabelText(/contact number/i), '1712345678');
    const creditInput = within(dialog).getByLabelText(/assigned credit/i);
    await userEvent.clear(creditInput);
    await userEvent.type(creditInput, '5');

    await userEvent.click(within(dialog).getByRole('button', { name: /^add$/i }));

    await waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const [postUrl, postBody] = postSpy.mock.calls[0];
    expect(postUrl).toMatch(/\/v1\/teachers$/);
    expect(postBody).toMatchObject({
      fullName: 'New Guy',
      email: 'new@u.edu',
      departmentCode: 'CSE',
      title: 'Professor',
      contactNumber: '+8801712345678',
      assignedCredit: 5,
    });

    expect(await screen.findByText('Teacher added')).toBeInTheDocument();
    expect(await screen.findByText('New Guy')).toBeInTheDocument();
  });

  it('shows error snackbar when DELETE fails', async () => {
    mockGetByUrl({
      '/v1/teachers': teachersPage(initialTeachers),
      '/v1/departments': { _embedded: { departments: mockDepartments } },
      '/v1/designations': { _embedded: { designations: mockDesignations } },
    });
    vi.spyOn(axios, 'delete').mockRejectedValue(new Error('boom'));
    renderPage();

    await screen.findByText('John Doe');
    await userEvent.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);

    await screen.findByText('Delete teacher?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    expect(await screen.findByText('Failed to delete teacher')).toBeInTheDocument();
  });
});
