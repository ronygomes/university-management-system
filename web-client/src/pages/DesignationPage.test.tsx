import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import axios from 'axios';
import DesignationPage from './DesignationPage';
import * as AuthContext from '../components/AuthContext';

function renderPage() {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated: true,
    token: null,
    username: null,
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
        <DesignationPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function halItem(id: number, title: string) {
  return {
    title,
    _links: { self: { href: `http://localhost:8100/v1/designations/${id}` } },
  };
}

const initialHalDesignations = [
  halItem(1, 'Lecturer'),
  halItem(2, 'Assistant Professor'),
];

describe('DesignationPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the Designations heading', () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: [] } } });
    renderPage();

    expect(screen.getByText('Designations')).toBeInTheDocument();
  });

  it('renders designation titles fetched from the API', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    renderPage();

    expect(await screen.findByText('Lecturer')).toBeInTheDocument();
    expect(screen.getByText('Assistant Professor')).toBeInTheDocument();
  });

  it('opens the confirmation dialog when Delete is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    expect(await screen.findByText('Delete designation?')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByText(/Lecturer/)).toBeInTheDocument();
  });

  it('does not call DELETE when Cancel is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await screen.findByText('Delete designation?');
    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(deleteSpy).not.toHaveBeenCalled();
  });

  it('sends DELETE to the correct URL, refreshes table, and shows success snackbar', async () => {
    let getCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      getCallCount++;
      const list = getCallCount === 1
        ? initialHalDesignations
        : initialHalDesignations.filter((d) => d.title !== 'Lecturer');
      return { data: { _embedded: { designations: list } } };
    });
    const deleteSpy = vi.spyOn(axios, 'delete').mockResolvedValue({});
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await screen.findByText('Delete designation?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    await waitFor(() => expect(deleteSpy).toHaveBeenCalledTimes(1));
    expect(deleteSpy).toHaveBeenCalledWith(expect.stringMatching(/\/v1\/designations\/1$/));

    expect(await screen.findByText('Designation deleted')).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('Lecturer')).not.toBeInTheDocument());
  });

  it('opens the edit dialog with prefilled value when Edit is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    expect(await screen.findByText('Edit designation')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/title/i)).toHaveValue('Lecturer');
  });

  it('sends PUT with the new title, refreshes table, shows success snackbar', async () => {
    let getCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      getCallCount++;
      const list = getCallCount === 1
        ? initialHalDesignations
        : [halItem(1, 'Senior Lecturer'), initialHalDesignations[1]];
      return { data: { _embedded: { designations: list } } };
    });
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    const titleInput = within(dialog).getByLabelText(/title/i);
    await userEvent.clear(titleInput);
    await userEvent.type(titleInput, 'Senior Lecturer');
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    await waitFor(() => expect(putSpy).toHaveBeenCalledTimes(1));
    expect(putSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/designations\/1$/),
      { title: 'Senior Lecturer' },
    );

    expect(await screen.findByText('Designation updated')).toBeInTheDocument();
    expect(await screen.findByText('Senior Lecturer')).toBeInTheDocument();
  });

  it('shows inline validation error when Title is cleared', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    const putSpy = vi.spyOn(axios, 'put').mockResolvedValue({});
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.clear(within(dialog).getByLabelText(/title/i));
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    expect(await screen.findByText('Title is required')).toBeInTheDocument();
    expect(putSpy).not.toHaveBeenCalled();
  });

  it('shows server errors in an Alert at top of dialog when PUT fails', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    vi.spyOn(axios, 'put').mockRejectedValue({
      isAxiosError: true,
      response: {
        data: {
          errors: [{ field: 'title', message: 'must not be blank' }],
        },
      },
    });
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);

    const dialog = await screen.findByRole('dialog');
    await userEvent.click(within(dialog).getByRole('button', { name: /update/i }));

    const alert = await within(dialog).findByRole('alert');
    expect(alert).toHaveTextContent('title: must not be blank');
  });

  it('opens an empty add dialog when Add Designation is clicked', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getByRole('button', { name: /add designation/i }));

    expect(await screen.findByText('Add designation')).toBeInTheDocument();
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByLabelText(/title/i)).toHaveValue('');
    expect(within(dialog).getByRole('button', { name: /^add$/i })).toBeInTheDocument();
  });

  it('sends POST when adding, refreshes table, and shows success snackbar', async () => {
    let getCallCount = 0;
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      getCallCount++;
      const list = getCallCount === 1
        ? initialHalDesignations
        : [...initialHalDesignations, halItem(99, 'Professor')];
      return { data: { _embedded: { designations: list } } };
    });
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getByRole('button', { name: /add designation/i }));

    const dialog = await screen.findByRole('dialog');
    await userEvent.type(within(dialog).getByLabelText(/title/i), 'Professor');
    await userEvent.click(within(dialog).getByRole('button', { name: /^add$/i }));

    await waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    expect(postSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/v1\/designations$/),
      { title: 'Professor' },
    );

    expect(await screen.findByText('Designation added')).toBeInTheDocument();
    expect(await screen.findByText('Professor')).toBeInTheDocument();
  });

  it('shows error snackbar when DELETE fails', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { designations: initialHalDesignations } } });
    vi.spyOn(axios, 'delete').mockRejectedValue(new Error('boom'));
    renderPage();

    await screen.findByText('Lecturer');
    await userEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);

    await screen.findByText('Delete designation?');
    const dialogDelete = screen.getAllByRole('button', { name: /^delete$/i }).at(-1)!;
    await userEvent.click(dialogDelete);

    expect(await screen.findByText('Failed to delete designation')).toBeInTheDocument();
  });
});
