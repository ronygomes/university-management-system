import axios, { isAxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  InputAdornment,
  MenuItem,
  Paper,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
} from '@mui/material';
import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';
import { DEFAULT_ROWS_PER_PAGE, ROWS_PER_PAGE_OPTIONS, tableHeadRowSx } from '../components/tableStyles';

const TEACHER_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/teachers`;
const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;
const DESIGNATION_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/designations`;

const BANGLADESH_PHONE_CODE = '+880';
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const CONTACT_NUMBER_SUFFIX_REGEX = /^\d{10}$/;

type TeacherListItem = {
  id: number;
  fullName: string;
  title: string;
  email: string;
  departmentCode: string;
};

type TeacherHalItem = {
  fullName: string;
  email: string;
  title: string;
  departmentCode: string;
  _links: { self: { href: string } };
};

type FullTeacher = TeacherListItem & {
  address: string;
  contactNumber: string;
  assignedCredit: number;
};

type TeacherFormData = {
  fullName: string;
  email: string;
  departmentCode: string;
  title: string;
  contactNumberSuffix: string;
  address: string;
  assignedCredit: number;
};

type CreateTeacherPayload = Omit<TeacherFormData, 'contactNumberSuffix'> & {
  contactNumber: string;
};

type Department = { code: string; name: string };
type DesignationHalItem = { title: string };

function extractIdFromSelfLink(href: string): number {
  return Number(href.split('/').pop());
}

type PagedTeachers = {
  content: TeacherListItem[];
  totalElements: number;
};

async function fetchTeachersPaged(page: number, size: number): Promise<PagedTeachers> {
  const response = await axios.get(`${TEACHER_ENDPOINT}/paged`, { params: { page, size } });
  const items: TeacherHalItem[] = response.data._embedded?.teachers ?? [];
  return {
    content: items.map((item) => ({
      id: extractIdFromSelfLink(item._links.self.href),
      fullName: item.fullName,
      title: item.title,
      email: item.email,
      departmentCode: item.departmentCode,
    })),
    totalElements: response.data.page?.totalElements ?? 0,
  };
}

async function fetchTeacher(id: number): Promise<FullTeacher> {
  const response = await axios.get(`${TEACHER_ENDPOINT}/${id}`);
  return {
    id,
    fullName: response.data.fullName,
    email: response.data.email,
    title: response.data.title,
    departmentCode: response.data.departmentCode,
    address: response.data.address ?? '',
    contactNumber: response.data.contactNumber ?? '',
    assignedCredit: response.data.assignedCredit ?? 0,
  };
}

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENT_ENDPOINT);
  return response.data._embedded?.departments ?? [];
}

async function fetchDesignationTitles(): Promise<string[]> {
  const response = await axios.get(DESIGNATION_ENDPOINT);
  const items: DesignationHalItem[] = response.data._embedded?.designations ?? [];
  return items.map((d) => d.title);
}

async function deleteTeacher(id: number): Promise<void> {
  await axios.delete(`${TEACHER_ENDPOINT}/${id}`);
}

async function updateTeacher(id: number, data: CreateTeacherPayload): Promise<void> {
  await axios.put(`${TEACHER_ENDPOINT}/${id}`, data);
}

async function createTeacher(data: CreateTeacherPayload): Promise<void> {
  await axios.post(TEACHER_ENDPOINT, data);
}

type ServerErrorMessage = { field: string; message: string };

interface TeacherFormProps {
  teacherId: number | null;
  initial: FullTeacher | null;
  onCancel: () => void;
  onSuccess: (mode: 'add' | 'edit') => void;
}

const TeacherForm = ({ teacherId, initial, onCancel, onSuccess }: TeacherFormProps) => {
  const queryClient = useQueryClient();
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const isEdit = teacherId !== null;

  const { data: departments = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });
  const { data: designationTitles = [] } = useQuery({
    queryKey: ['designations', 'titles'],
    queryFn: fetchDesignationTitles,
  });

  const stripPhonePrefix = (full: string): string =>
    full.startsWith(BANGLADESH_PHONE_CODE) ? full.slice(BANGLADESH_PHONE_CODE.length) : full;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TeacherFormData>({
    mode: 'onTouched',
    defaultValues: {
      fullName: initial?.fullName ?? '',
      email: initial?.email ?? '',
      departmentCode: initial?.departmentCode ?? '',
      title: initial?.title ?? '',
      contactNumberSuffix: initial ? stripPhonePrefix(initial.contactNumber) : '',
      address: initial?.address ?? '',
      assignedCredit: initial?.assignedCredit ?? 0,
    },
  });

  const { mutate, isPending } = useMutation({
    mutationFn: (payload: CreateTeacherPayload) =>
      isEdit ? updateTeacher(teacherId, payload) : createTeacher(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teachers'] });
      reset({});
      onSuccess(isEdit ? 'edit' : 'add');
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError(isEdit ? 'Failed to update teacher' : 'Failed to add teacher');
      }
    },
  });

  const onSubmit = (data: TeacherFormData) => {
    setServerErrors(null);
    setFallbackError(null);
    const { contactNumberSuffix, assignedCredit, ...rest } = data;
    const payload: CreateTeacherPayload = {
      ...rest,
      contactNumber: contactNumberSuffix
        ? `${BANGLADESH_PHONE_CODE}${contactNumberSuffix}`
        : '',
      assignedCredit: Number(assignedCredit),
    };
    mutate(payload);
  };

  return (
    <>
      <DialogTitle>{isEdit ? 'Edit teacher' : 'Add teacher'}</DialogTitle>
      <DialogContent>
        {serverErrors && serverErrors.length > 0 && (
          <Alert severity='error' sx={{ mb: 2, whiteSpace: 'pre-line' }}>
            {serverErrors
              .map((e) => (e.field === '*' ? e.message : `${e.field}: ${e.message}`))
              .join('\n')}
          </Alert>
        )}
        {fallbackError && (
          <Alert severity='error' sx={{ mb: 2 }}>{fallbackError}</Alert>
        )}
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label='Full Name'
            fullWidth
            required
            error={!!errors.fullName}
            helperText={errors.fullName?.message}
            {...register('fullName', {
              required: 'Full name is required',
              maxLength: { value: 200, message: 'Max 200 characters' },
            })}
          />
          <TextField
            label='Email'
            fullWidth
            required
            error={!!errors.email}
            helperText={errors.email?.message}
            {...register('email', {
              required: 'Email is required',
              minLength: { value: 5, message: 'Min 5 characters' },
              maxLength: { value: 100, message: 'Max 100 characters' },
              pattern: { value: EMAIL_REGEX, message: 'Invalid email format' },
            })}
          />
          <TextField
            select
            label='Department'
            fullWidth
            required
            defaultValue={initial?.departmentCode ?? ''}
            error={!!errors.departmentCode}
            helperText={errors.departmentCode?.message}
            {...register('departmentCode', { required: 'Department is required' })}
          >
            {departments.map((dept) => (
              <MenuItem key={dept.code} value={dept.code}>
                {dept.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label='Designation'
            fullWidth
            required
            defaultValue={initial?.title ?? ''}
            error={!!errors.title}
            helperText={errors.title?.message}
            {...register('title', { required: 'Designation is required' })}
          >
            {designationTitles.map((t) => (
              <MenuItem key={t} value={t}>
                {t}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label='Contact Number'
            fullWidth
            placeholder='10 digit number'
            error={!!errors.contactNumberSuffix}
            helperText={errors.contactNumberSuffix?.message}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position='start'>{BANGLADESH_PHONE_CODE}</InputAdornment>
                ),
              },
            }}
            {...register('contactNumberSuffix', {
              pattern: { value: CONTACT_NUMBER_SUFFIX_REGEX, message: 'Must be 10 digits' },
            })}
          />
          <TextField
            label='Address'
            fullWidth
            multiline
            rows={3}
            error={!!errors.address}
            helperText={errors.address?.message}
            {...register('address', {
              maxLength: { value: 1000, message: 'Max 1000 characters' },
            })}
          />
          <TextField
            label='Assigned Credit'
            fullWidth
            required
            type='number'
            inputProps={{ min: 0, max: 100, step: 0.5 }}
            error={!!errors.assignedCredit}
            helperText={errors.assignedCredit?.message}
            {...register('assignedCredit', {
              required: 'Assigned credit is required',
              min: { value: 0, message: 'Min 0' },
              max: { value: 100, message: 'Max 100' },
              valueAsNumber: true,
            })}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={isPending}>
          Cancel
        </Button>
        <Button variant='contained' onClick={handleSubmit(onSubmit)} disabled={isPending}>
          {isEdit ? 'Update' : 'Add'}
        </Button>
      </DialogActions>
    </>
  );
};

interface TeacherFormDialogBodyProps {
  teacherId: number | null;
  onCancel: () => void;
  onSuccess: (mode: 'add' | 'edit') => void;
}

const TeacherFormDialogBody = ({ teacherId, onCancel, onSuccess }: TeacherFormDialogBodyProps) => {
  const isEdit = teacherId !== null;

  const { data: fullTeacher, isLoading } = useQuery({
    queryKey: ['teachers', teacherId],
    queryFn: () => fetchTeacher(teacherId as number),
    enabled: isEdit,
  });

  if (isEdit && isLoading) {
    return (
      <>
        <DialogTitle>Edit teacher</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        </DialogContent>
      </>
    );
  }

  return (
    <TeacherForm
      teacherId={teacherId}
      initial={fullTeacher ?? null}
      onCancel={onCancel}
      onSuccess={onSuccess}
    />
  );
};

const TeacherPage = () => {
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<TeacherListItem | null>(null);
  const [pendingEditId, setPendingEditId] = useState<number | null>(null);
  const [addOpen, setAddOpen] = useState(false);
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(DEFAULT_ROWS_PER_PAGE);

  const { data } = useQuery({
    queryKey: ['teachers', 'paged', page, rowsPerPage],
    queryFn: () => fetchTeachersPaged(page, rowsPerPage),
    placeholderData: keepPreviousData,
  });
  const teachers = data?.content ?? [];
  const totalElements = data?.totalElements ?? 0;

  const { mutate: triggerDelete, isPending: isDeletePending } = useMutation({
    mutationFn: deleteTeacher,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teachers'] });
      setPendingDelete(null);
      setSuccessMessage('Teacher deleted');
      setSuccessOpen(true);
    },
    onError: () => {
      setPendingDelete(null);
      setErrorOpen(true);
    },
  });

  const onConfirmDelete = () => {
    if (pendingDelete) {
      triggerDelete(pendingDelete.id);
    }
  };

  return (
    <ProtectedPage>
      <ContentWrapper>
        <h1>Teachers</h1>
        <Stack direction='row' justifyContent='flex-end' sx={{ mb: 2 }}>
          <Button variant='contained' onClick={() => setAddOpen(true)}>
            Add Teacher
          </Button>
        </Stack>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={tableHeadRowSx}>
                <TableCell>Name</TableCell>
                <TableCell>Title</TableCell>
                <TableCell align='right'>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {teachers.map((teacher) => (
                <TableRow key={teacher.id}>
                  <TableCell>{teacher.fullName}</TableCell>
                  <TableCell>{teacher.title}</TableCell>
                  <TableCell align='right'>
                    <Button
                      variant='outlined'
                      size='small'
                      sx={{ mr: 1 }}
                      onClick={() => setPendingEditId(teacher.id)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='contained'
                      color='error'
                      size='small'
                      onClick={() => setPendingDelete(teacher)}
                    >
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component='div'
          count={totalElements}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={ROWS_PER_PAGE_OPTIONS}
        />

        <Dialog open={pendingDelete !== null} onClose={() => setPendingDelete(null)}>
          <DialogTitle>Delete teacher?</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Are you sure you want to delete &quot;{pendingDelete?.fullName}&quot;? This cannot be undone.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPendingDelete(null)} disabled={isDeletePending}>
              Cancel
            </Button>
            <Button color='error' onClick={onConfirmDelete} disabled={isDeletePending}>
              Delete
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={pendingEditId !== null || addOpen}
          onClose={() => {
            setPendingEditId(null);
            setAddOpen(false);
          }}
          fullWidth
          maxWidth='sm'
        >
          {(pendingEditId !== null || addOpen) && (
            <TeacherFormDialogBody
              teacherId={pendingEditId}
              onCancel={() => {
                setPendingEditId(null);
                setAddOpen(false);
              }}
              onSuccess={(mode) => {
                setPendingEditId(null);
                setAddOpen(false);
                setSuccessMessage(mode === 'edit' ? 'Teacher updated' : 'Teacher added');
                setSuccessOpen(true);
              }}
            />
          )}
        </Dialog>

        <Snackbar
          open={successOpen}
          autoHideDuration={4000}
          onClose={() => setSuccessOpen(false)}
          message={successMessage}
        />
        <Snackbar
          open={errorOpen}
          autoHideDuration={4000}
          onClose={() => setErrorOpen(false)}
          message='Failed to delete teacher'
        />
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default TeacherPage;
