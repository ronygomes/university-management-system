import axios, { isAxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
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
  Typography,
} from '@mui/material';
import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined';
import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';
import { DEFAULT_ROWS_PER_PAGE, ROWS_PER_PAGE_OPTIONS, tableHeadRowSx } from '../components/tableStyles';

const COURSE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/courses`;
const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;
const TEACHER_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/teachers`;

type Semester =
  | 'FIRST_YEAR_FIRST' | 'FIRST_YEAR_SECOND'
  | 'SECOND_YEAR_FIRST' | 'SECOND_YEAR_SECOND'
  | 'THIRD_YEAR_FIRST' | 'THIRD_YEAR_SECOND'
  | 'FOURTH_YEAR_FIRST' | 'FOURTH_YEAR_SECOND'
  | 'FIFTH_YEAR_FIRST' | 'FIFTH_YEAR_SECOND';

const SEMESTERS: { value: Semester; label: string }[] = [
  { value: 'FIRST_YEAR_FIRST', label: '1st Year - 1st Semester' },
  { value: 'FIRST_YEAR_SECOND', label: '1st Year - 2nd Semester' },
  { value: 'SECOND_YEAR_FIRST', label: '2nd Year - 1st Semester' },
  { value: 'SECOND_YEAR_SECOND', label: '2nd Year - 2nd Semester' },
  { value: 'THIRD_YEAR_FIRST', label: '3rd Year - 1st Semester' },
  { value: 'THIRD_YEAR_SECOND', label: '3rd Year - 2nd Semester' },
  { value: 'FOURTH_YEAR_FIRST', label: '4th Year - 1st Semester' },
  { value: 'FOURTH_YEAR_SECOND', label: '4th Year - 2nd Semester' },
  { value: 'FIFTH_YEAR_FIRST', label: '5th Year - 1st Semester' },
  { value: 'FIFTH_YEAR_SECOND', label: '5th Year - 2nd Semester' },
];

const SEMESTER_LABEL: Record<Semester, string> =
  Object.fromEntries(SEMESTERS.map((s) => [s.value, s.label])) as Record<Semester, string>;

type Course = {
  id: number;
  title: string;
  name: string;
  credit: number;
  description: string;
  departmentCode: string;
  semester: Semester;
  instructorId: number | null;
};

type CourseFormData = {
  title: string;
  name: string;
  credit: number;
  description: string;
  departmentCode: string;
  semester: Semester | '';
  instructorId: number | '';
};

type CoursePayload = Omit<CourseFormData, 'instructorId' | 'semester'> & {
  semester: Semester;
  instructorId: number | null;
};

type Department = { code: string; name: string };
type TeacherHalItem = {
  fullName: string;
  _links: { self: { href: string } };
};
type TeacherListItem = { id: number; fullName: string };

function extractIdFromSelfLink(href: string): number {
  return Number(href.split('/').pop());
}

type PagedCourses = {
  content: Course[];
  totalElements: number;
};

async function fetchCoursesPaged(
  page: number,
  size: number,
  departmentCode: string,
  semester: Semester | '',
): Promise<PagedCourses> {
  const params: Record<string, string | number> = { page, size };
  if (departmentCode) params.departmentCode = departmentCode;
  if (semester) params.semester = semester;

  const response = await axios.get(COURSE_ENDPOINT, { params });
  return {
    content: response.data.content ?? [],
    totalElements: response.data.totalElements ?? 0,
  };
}

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENT_ENDPOINT);
  return response.data._embedded?.departments ?? [];
}

async function fetchTeachers(): Promise<TeacherListItem[]> {
  const response = await axios.get(TEACHER_ENDPOINT);
  const items: TeacherHalItem[] = response.data._embedded?.teachers ?? [];
  return items.map((item) => ({
    id: extractIdFromSelfLink(item._links.self.href),
    fullName: item.fullName,
  }));
}

async function deleteCourse(id: number): Promise<void> {
  await axios.delete(`${COURSE_ENDPOINT}/${id}`);
}

async function updateCourse(id: number, data: CoursePayload): Promise<void> {
  await axios.put(`${COURSE_ENDPOINT}/${id}`, data);
}

async function createCourse(data: CoursePayload): Promise<void> {
  await axios.post(COURSE_ENDPOINT, data);
}

type ServerErrorMessage = { field: string; message: string };

interface CourseFormDialogBodyProps {
  course: Course | null;
  onCancel: () => void;
  onSuccess: (mode: 'add' | 'edit') => void;
}

const CourseFormDialogBody = ({ course, onCancel, onSuccess }: CourseFormDialogBodyProps) => {
  const queryClient = useQueryClient();
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const isEdit = course !== null;

  const { data: departments = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });
  const { data: teachers = [] } = useQuery({
    queryKey: ['teachers'],
    queryFn: fetchTeachers,
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CourseFormData>({
    mode: 'onTouched',
    defaultValues: {
      title: course?.title ?? '',
      name: course?.name ?? '',
      credit: course?.credit ?? 0,
      description: course?.description ?? '',
      departmentCode: course?.departmentCode ?? '',
      semester: course?.semester ?? '',
      instructorId: course?.instructorId ?? '',
    },
  });

  const { mutate, isPending } = useMutation({
    mutationFn: (payload: CoursePayload) =>
      isEdit ? updateCourse(course.id, payload) : createCourse(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['courses'] });
      reset({});
      onSuccess(isEdit ? 'edit' : 'add');
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError(isEdit ? 'Failed to update course' : 'Failed to add course');
      }
    },
  });

  const onSubmit = (data: CourseFormData) => {
    setServerErrors(null);
    setFallbackError(null);
    const payload: CoursePayload = {
      title: data.title,
      name: data.name,
      credit: Number(data.credit),
      description: data.description,
      departmentCode: data.departmentCode,
      semester: data.semester as Semester,
      instructorId: data.instructorId === '' ? null : Number(data.instructorId),
    };
    mutate(payload);
  };

  return (
    <>
      <DialogTitle>{isEdit ? 'Edit course' : 'Add course'}</DialogTitle>
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
            label='Title'
            fullWidth
            required
            error={!!errors.title}
            helperText={errors.title?.message}
            {...register('title', {
              required: 'Title is required',
              maxLength: { value: 20, message: 'Max 20 characters' },
            })}
          />
          <TextField
            label='Name'
            fullWidth
            required
            error={!!errors.name}
            helperText={errors.name?.message}
            {...register('name', {
              required: 'Name is required',
              maxLength: { value: 200, message: 'Max 200 characters' },
            })}
          />
          <TextField
            label='Credit'
            fullWidth
            required
            type='number'
            inputProps={{ min: 0, max: 5, step: 0.5 }}
            error={!!errors.credit}
            helperText={errors.credit?.message}
            {...register('credit', {
              required: 'Credit is required',
              min: { value: 0, message: 'Min 0.0' },
              max: { value: 5, message: 'Max 5.0' },
              valueAsNumber: true,
            })}
          />
          <TextField
            select
            label='Department'
            fullWidth
            required
            defaultValue={course?.departmentCode ?? ''}
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
            label='Semester'
            fullWidth
            required
            defaultValue={course?.semester ?? ''}
            error={!!errors.semester}
            helperText={errors.semester?.message}
            {...register('semester', { required: 'Semester is required' })}
          >
            {SEMESTERS.map((s) => (
              <MenuItem key={s.value} value={s.value}>
                {s.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label='Instructor'
            fullWidth
            defaultValue={course?.instructorId ?? ''}
            error={!!errors.instructorId}
            helperText={errors.instructorId?.message}
            {...register('instructorId')}
          >
            <MenuItem value=''>None</MenuItem>
            {teachers.map((t) => (
              <MenuItem key={t.id} value={t.id}>
                {t.fullName}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label='Description'
            fullWidth
            multiline
            rows={3}
            error={!!errors.description}
            helperText={errors.description?.message}
            {...register('description', {
              maxLength: { value: 2000, message: 'Max 2000 characters' },
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

const CoursePage = () => {
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<Course | null>(null);
  const [pendingEdit, setPendingEdit] = useState<Course | null>(null);
  const [addOpen, setAddOpen] = useState(false);
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState<string>('');
  const [semesterFilter, setSemesterFilter] = useState<Semester | ''>('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(DEFAULT_ROWS_PER_PAGE);

  const { data } = useQuery({
    queryKey: ['courses', 'paged', page, rowsPerPage, departmentFilter, semesterFilter],
    queryFn: () => fetchCoursesPaged(page, rowsPerPage, departmentFilter, semesterFilter),
    placeholderData: keepPreviousData,
  });
  const courses = data?.content ?? [];
  const totalElements = data?.totalElements ?? 0;

  const { data: departmentList = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });

  const { mutate: triggerDelete, isPending: isDeletePending } = useMutation({
    mutationFn: deleteCourse,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['courses'] });
      setPendingDelete(null);
      setSuccessMessage('Course deleted');
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
        <h1>Courses</h1>
        <Stack direction='row' justifyContent='flex-end' sx={{ mb: 2 }}>
          <Button variant='contained' onClick={() => setAddOpen(true)}>
            Add Course
          </Button>
        </Stack>
        <Stack direction='row' spacing={2} alignItems='center' sx={{ mb: 2 }}>
          <Stack direction='row' spacing={0.5} alignItems='center' sx={{ color: 'text.secondary' }}>
            <FilterAltOutlinedIcon fontSize='small' />
            <Typography variant='body2'>Filters</Typography>
          </Stack>
          <TextField
            select
            label='Department'
            size='small'
            sx={{ minWidth: 200 }}
            value={departmentFilter}
            onChange={(e) => {
              setDepartmentFilter(e.target.value);
              setPage(0);
            }}
          >
            <MenuItem value=''>All</MenuItem>
            {departmentList.map((d) => (
              <MenuItem key={d.code} value={d.code}>{d.name}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label='Semester'
            size='small'
            sx={{ minWidth: 220 }}
            value={semesterFilter}
            onChange={(e) => {
              setSemesterFilter(e.target.value as Semester | '');
              setPage(0);
            }}
          >
            <MenuItem value=''>All</MenuItem>
            {SEMESTERS.map((s) => (
              <MenuItem key={s.value} value={s.value}>{s.label}</MenuItem>
            ))}
          </TextField>
        </Stack>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={tableHeadRowSx}>
                <TableCell>Name</TableCell>
                <TableCell>Semester</TableCell>
                <TableCell>Credit</TableCell>
                <TableCell align='right'>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {courses.map((course) => (
                <TableRow key={course.id}>
                  <TableCell>{course.name}</TableCell>
                  <TableCell>{SEMESTER_LABEL[course.semester] ?? course.semester}</TableCell>
                  <TableCell>{course.credit}</TableCell>
                  <TableCell align='right'>
                    <Button
                      variant='outlined'
                      size='small'
                      sx={{ mr: 1 }}
                      onClick={() => setPendingEdit(course)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='contained'
                      color='error'
                      size='small'
                      onClick={() => setPendingDelete(course)}
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
          <DialogTitle>Delete course?</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Are you sure you want to delete &quot;{pendingDelete?.name}&quot;? This cannot be undone.
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
          open={pendingEdit !== null || addOpen}
          onClose={() => {
            setPendingEdit(null);
            setAddOpen(false);
          }}
          fullWidth
          maxWidth='sm'
        >
          {(pendingEdit !== null || addOpen) && (
            <CourseFormDialogBody
              course={pendingEdit}
              onCancel={() => {
                setPendingEdit(null);
                setAddOpen(false);
              }}
              onSuccess={(mode) => {
                setPendingEdit(null);
                setAddOpen(false);
                setSuccessMessage(mode === 'edit' ? 'Course updated' : 'Course added');
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
          message='Failed to delete course'
        />
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default CoursePage;
