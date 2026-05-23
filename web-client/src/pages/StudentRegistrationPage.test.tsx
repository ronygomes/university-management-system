import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import axios from 'axios';
import StudentRegistrationPage from './StudentRegistrationPage';
import * as AuthContext from '../components/AuthContext';

function renderPage() {
  vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
    isAuthenticated: true,
    token: null,
    username: null,
    email: null,
    role: null,
    loginHandler: vi.fn(),
    logoutHandler: vi.fn(),
  });

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <StudentRegistrationPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const mockDepartments = [
  { code: 'CSE', name: 'Computer Science' },
  { code: 'EEE', name: 'Electrical Engineering' },
];

describe('StudentRegistrationPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders all form fields', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: mockDepartments } } });
    renderPage();

    expect(await screen.findByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/department/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/contact number/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/address/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });

  it('renders departments in the dropdown', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: mockDepartments } } });
    renderPage();

    await userEvent.click(await screen.findByLabelText(/department/i));

    expect(await screen.findByText('Computer Science')).toBeInTheDocument();
    expect(screen.getByText('Electrical Engineering')).toBeInTheDocument();
  });

  it('shows validation errors when submitting empty required fields', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    renderPage();

    await userEvent.click(await screen.findByRole('button', { name: /register/i }));

    expect(await screen.findByText('Full name is required')).toBeInTheDocument();
    expect(screen.getByText('Email is required')).toBeInTheDocument();
    expect(screen.getByText('Department is required')).toBeInTheDocument();
  });

  it('shows validation error for invalid email format', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    renderPage();

    await userEvent.type(await screen.findByLabelText(/email/i), 'not-an-email');
    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText('Invalid email format')).toBeInTheDocument();
  });

  it('shows validation error for invalid contact number format', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    renderPage();

    await userEvent.type(await screen.findByLabelText(/contact number/i), '12345');
    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText('Must be 10 digits')).toBeInTheDocument();
  });

  it('shows success snackbar after successful submission', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: mockDepartments } } });
    vi.spyOn(axios, 'post').mockResolvedValue({});
    renderPage();

    await userEvent.type(await screen.findByLabelText(/full name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');
    await userEvent.click(screen.getByLabelText(/department/i));
    await userEvent.click(await screen.findByText('Computer Science'));
    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText('Student registered successfully')).toBeInTheDocument();
  });

  it('does not reset student form fields when a valid education is added to the list', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    vi.spyOn(axios, 'post').mockImplementation(async (url) => {
      if (typeof url === 'string' && url.includes('/v1/upload')) {
        return { data: { name: 'cert.pdf', uploadPath: 'uuid-1', fileSize: 4, filetype: 'application/pdf' } };
      }
      return {};
    });
    renderPage();

    await userEvent.type(await screen.findByLabelText(/full name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');

    await userEvent.click(screen.getByLabelText(/exam/i));
    await userEvent.click(await screen.findByText('A Level'));
    await userEvent.click(screen.getByLabelText(/grade/i));
    await userEvent.click(await screen.findByText('A+'));
    await userEvent.type(screen.getByLabelText(/cgpa/i), '3.5');

    const file = new File(['data'], 'cert.pdf', { type: 'application/pdf' });
    await userEvent.upload(screen.getByLabelText(/certificate/i), file);

    const addButton = await screen.findByRole('button', { name: /add to list/i });
    await waitFor(() => expect(addButton).toBeEnabled());
    await userEvent.click(addButton);

    expect(screen.getByLabelText(/full name/i)).toHaveValue('John Doe');
    expect(screen.getByLabelText(/email/i)).toHaveValue('john@example.com');
  });

  it('shows spinner while certificate is uploading and enables Add to List on success', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    let resolveUpload: (value: unknown) => void = () => {};
    vi.spyOn(axios, 'post').mockImplementation(
      () => new Promise((resolve) => { resolveUpload = resolve; }),
    );
    renderPage();

    await screen.findByLabelText(/full name/i);
    const file = new File(['data'], 'cert.pdf', { type: 'application/pdf' });
    await userEvent.upload(screen.getByLabelText(/certificate/i), file);

    expect(await screen.findByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add to list/i })).toBeDisabled();

    resolveUpload({ data: { name: 'cert.pdf', uploadPath: 'uuid-1', fileSize: 4, filetype: 'application/pdf' } });

    await waitFor(() => expect(screen.queryByRole('progressbar')).not.toBeInTheDocument());
  });

  it('shows inline error when certificate upload fails', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: [] } } });
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('upload failed'));
    renderPage();

    await screen.findByLabelText(/full name/i);
    const file = new File(['data'], 'cert.pdf', { type: 'application/pdf' });
    await userEvent.upload(screen.getByLabelText(/certificate/i), file);

    expect(await screen.findByText('Upload failed')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add to list/i })).toBeDisabled();
  });

  it('submits student with certificatePath from upload response', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: mockDepartments } } });
    let capturedStudentPayload: unknown;
    vi.spyOn(axios, 'post').mockImplementation(async (url, data) => {
      if (typeof url === 'string' && url.includes('/v1/upload')) {
        return { data: { name: 'cert.pdf', uploadPath: 'uuid-from-server', fileSize: 4, filetype: 'application/pdf' } };
      }
      if (typeof url === 'string' && url.includes('/v1/students')) {
        capturedStudentPayload = data;
        return {};
      }
      return {};
    });
    renderPage();

    await userEvent.type(await screen.findByLabelText(/full name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');
    await userEvent.click(screen.getByLabelText(/department/i));
    await userEvent.click(await screen.findByText('Computer Science'));

    await userEvent.click(screen.getByLabelText(/exam/i));
    await userEvent.click(await screen.findByText('A Level'));
    await userEvent.click(screen.getByLabelText(/grade/i));
    await userEvent.click(await screen.findByText('A+'));
    await userEvent.type(screen.getByLabelText(/cgpa/i), '3.5');

    const file = new File(['data'], 'cert.pdf', { type: 'application/pdf' });
    await userEvent.upload(screen.getByLabelText(/certificate/i), file);

    const addButton = await screen.findByRole('button', { name: /add to list/i });
    await waitFor(() => expect(addButton).toBeEnabled());
    await userEvent.click(addButton);

    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => expect(capturedStudentPayload).toBeDefined());
    const payload = capturedStudentPayload as { educations: Array<{ certificateFileName: string; certificatePath: string }> };
    expect(payload.educations).toHaveLength(1);
    expect(payload.educations[0].certificatePath).toBe('uuid-from-server');
    expect(payload.educations[0].certificateFileName).toBe('cert.pdf');
  });

  it('shows error snackbar when submission fails', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { _embedded: { departments: mockDepartments } } });
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('Server error'));
    renderPage();

    await userEvent.type(await screen.findByLabelText(/full name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');
    await userEvent.click(screen.getByLabelText(/department/i));
    await userEvent.click(await screen.findByText('Computer Science'));
    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText('Registration failed. Please try again.')).toBeInTheDocument();
  });
});
