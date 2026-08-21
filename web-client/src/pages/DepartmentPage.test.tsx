import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import DepartmentPage from './DepartmentPage';
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
        <DepartmentPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const initialDepartments = [
  { code: 'CSE', name: 'Computer Science' },
  { code: 'EEE', name: 'Electrical Engineering' },
];

function pagedData(list: { code: string; name: string }[]) {
  return {
    data: {
      _embedded: { departments: list },
      page: { size: 5, totalElements: list.length, totalPages: 1, number: 0 },
    },
  };
}

describe('DepartmentPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the Departments heading', () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData([]));
    renderPage();

    expect(screen.getByText('Departments')).toBeInTheDocument();
  });

  it('renders department names fetched from the API', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    renderPage();

    expect(await screen.findByText('Computer Science')).toBeInTheDocument();
    expect(screen.getByText('Electrical Engineering')).toBeInTheDocument();
  });

  it('requests the paged endpoint and loads the next page on navigation', async () => {
    let call = 0;
    const getSpy = vi.spyOn(axios, 'get').mockImplementation(async () => {
      call++;
      const list = call === 1
        ? [{ code: 'CSE', name: 'Computer Science' }]
        : [{ code: 'MATH', name: 'Mathematics' }];
      return {
        data: {
          _embedded: { departments: list },
          page: { size: 10, totalElements: 20, totalPages: 2, number: call - 1 },
        },
      };
    });
    renderPage();

    await screen.findByText('Computer Science');
    expect(getSpy).toHaveBeenNthCalledWith(
      1,
      expect.stringMatching(/\/v1\/departments\/paged$/),
      { params: { page: 0, size: 10 } },
    );

    await userEvent.click(screen.getByRole('button', { name: /next page/i }));

    expect(await screen.findByText('Mathematics')).toBeInTheDocument();
    expect(getSpy).toHaveBeenNthCalledWith(
      2,
      expect.stringMatching(/\/v1\/departments\/paged$/),
      { params: { page: 1, size: 10 } },
    );
  });

  it('opens the confirmation dialog when Delete is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    renderPage();

    await screen.findByText('Computer Science');
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    await userEvent.click(deleteButtons[0]);

    expect(await screen.findByText('Delete department?')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByText(/Computer Science/)).toBeInTheDocument();
  });

  it('does not call DELETE when Cancel is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await screen.findByText('Delete department?');
    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(deleteSpy).not.toHaveBeenCalled();
  });

  it('sends DELETE to the correct URL, refreshes table, and shows success snackbar', async () => {
    let getCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      getCallCount++;
      const list = getCallCount === 1
        ? initialDepartments
        : initialDepartments.filter((d) => d.code !== 'CSE');
      return pagedData(list);
    });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await screen.findByText('Delete department?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    await waitFor(() => expect(deleteSpy).toHaveBeenCalledTimes(1));
    expect(deleteSpy).toHaveBeenCalledWith(expect.stringMatching(/\/v1\/departments\/CSE$/));

    expect(await screen.findByText('Department deleted')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('Computer Science')).not.toBeInTheDocument());
    expect(screen.getByText('Electrical Engineering')).toBeInTheDocument();
  });

  it('opens the edit dialog with prefilled values when Edit is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    expect(await screen.findByText('Edit department')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/code/i)).toHaveValue('CSE');
    expect(within(dialog).getByLabelText(/name/i)).toHaveValue('Computer Science');
  });

  it('sends PUT with new values, refreshes table, and shows success snackbar', async () => {
    let getCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      getCallCount++;
      const list = getCallCount === 1
        ? initialDepartments
        : [{ code: 'CSE', name: 'Computer Science Updated' }, initialDepartments[1]];
      return pagedData(list);
    });
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    const nameInput = within(dialog).getByLabelText(/name/i);
    await userEvent.clear(nameInput);
    await userEvent.type(nameInput, 'Computer Science Updated');
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    await waitFor(() => expect(putSpy).toHaveBeenCalledTimes(1));
    expect(putSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/departments\/CSE$/),
      { code: 'CSE', name: 'Computer Science Updated' },
    );

    expect(await screen.findByText('Department updated')).toBeInTheDocument();
    expect(await screen.findByText('Computer Science Updated')).toBeInTheDocument();
  });

  it('shows inline validation error when Name is cleared', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.clear(within(dialog).getByLabelText(/name/i));
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    expect(await screen.findByText('Name is required')).toBeInTheDocument();
    expect(putSpy).not.toHaveBeenCalled();
  });

  it('shows server errors in an Alert at top of dialog when PUT fails', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    vi.spyOn(axios, 'put').mockRejectedValue({
      isAxiosError: true,
      response: {
        data: {
          errors: [
            { field: 'code', message: 'Code already in use' },
            { field: 'name', message: 'must not be blank' },
          ],
        },
      },
    });
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    const alert = await within(dialog).findByRole('alert');
    expect(alert).toHaveTextContent('code: Code already in use');
    expect(alert).toHaveTextContent('name: must not be blank');
  });

  it('opens an empty add dialog when Add Department is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getByRole('button', { name: /add department/i }));

    expect(await screen.findByText('Add department')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/code/i)).toHaveValue('');
    expect(within(dialog).getByLabelText(/name/i)).toHaveValue('');
    expect(within(dialog).getByRole('button', { name: /^add$/i })).toBeInTheDocument();
  });

  it('sends POST when adding, refreshes table, and shows success snackbar', async () => {
    let getCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      getCallCount++;
      const list = getCallCount === 1
        ? initialDepartments
        : [...initialDepartments, { code: 'MATH', name: 'Mathematics' }];
      return pagedData(list);
    });
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getByRole('button', { name: /add department/i }));

    const dialog = await screen.findByRole('dialog');
    await userEvent.type(within(dialog).getByLabelText(/code/i), 'MATH');
    await userEvent.type(within(dialog).getByLabelText(/name/i), 'Mathematics');
    await userEvent.click(within(dialog).getByRole('button', { name: /^add$/i }));

    await waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    expect(postSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/departments$/),
      { code: 'MATH', name: 'Mathematics' },
    );

    expect(await screen.findByText('Department added')).toBeInTheDocument();
    expect(await screen.findByText('Mathematics')).toBeInTheDocument();
  });

  it('shows error snackbar when DELETE fails', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue(pagedData(initialDepartments));
    vi.spyOn(axios, 'delete').mockRejectedValue(new Error('boom'));
    renderPage();

    await screen.findByText('Computer Science');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await screen.findByText('Delete department?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    expect(await screen.findByText('Failed to delete department')).toBeInTheDocument();
  });
});
