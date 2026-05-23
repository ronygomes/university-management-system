import axios, { isAxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  ListItemText,
  MenuItem,
  Paper,
  Select,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
} from '@mui/material';
import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';

const SCHEDULE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/schedules`;
const COURSE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/courses`;
const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;

type Semester =
  | 'FIRST_YEAR_FIRST' | 'FIRST_YEAR_SECOND'
  | 'SECOND_YEAR_FIRST' | 'SECOND_YEAR_SECOND'
  | 'THIRD_YEAR_FIRST' | 'THIRD_YEAR_SECOND'
  | 'FOURTH_YEAR_FIRST' | 'FOURTH_YEAR_SECOND'
  | 'FIFTH_YEAR_FIRST' | 'FIFTH_YEAR_SECOND';

type Building = 'BUILDING_1' | 'BUILDING_2';
type Day = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

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

const BUILDINGS: { value: Building; label: string }[] = [
  { value: 'BUILDING_1', label: 'Building 1' },
  { value: 'BUILDING_2', label: 'Building 2' },
];
const BUILDING_LABEL: Record<Building, string> =
  Object.fromEntries(BUILDINGS.map((b) => [b.value, b.label])) as Record<Building, string>;

const DAYS: Day[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_SHORT: Record<Day, string> = {
  MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu',
  FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun',
};

type Department = { code: string; name: string };
type Course = {
  id: number;
  title: string;
  name: string;
  departmentCode: string;
  semester: Semester;
};

type Schedule = {
  id: number;
  courseId: number;
  department: Department;
  semester: Semester;
  building: Building;
  roomNumber: string;
  days: Day[];
  startDate: string;
  endDate: string;
  enrollmentOpen: boolean;
};

type ScheduleFormData = {
  departmentCode: string;
  semester: Semester | '';
  courseId: number | '';
  building: Building | '';
  roomNumber: string;
  days: Day[];
  startDate: string;
  endDate: string;
};

type SchedulePayload = {
  departmentCode: string;
  semester: Semester;
  courseId: number;
  building: Building;
  roomNumber: string;
  days: Day[];
  startDate: string;
  endDate: string;
  enrollmentOpen: boolean;
};

async function fetchSchedules(): Promise<Schedule[]> {
  const response = await axios.get<Schedule[]>(SCHEDULE_ENDPOINT);
  return response.data;
}

async function fetchCourses(): Promise<Course[]> {
  const response = await axios.get<Course[]>(COURSE_ENDPOINT);
  return response.data;
}

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENT_ENDPOINT);
  return response.data._embedded?.departments ?? [];
}

async function deleteSchedule(id: number): Promise<void> {
  await axios.delete(`${SCHEDULE_ENDPOINT}/${id}`);
}

async function setScheduleEnrollmentOpen(id: number, open: boolean): Promise<void> {
  await axios.put(`${SCHEDULE_ENDPOINT}/${id}/enrollment-open`, null, { params: { open } });
}

async function updateSchedule(id: number, data: SchedulePayload): Promise<void> {
  await axios.put(`${SCHEDULE_ENDPOINT}/${id}`, data);
}

async function createSchedule(data: SchedulePayload): Promise<void> {
  await axios.post(SCHEDULE_ENDPOINT, data);
}

type ServerErrorMessage = { field: string; message: string };

interface ScheduleFormDialogBodyProps {
  schedule: Schedule | null;
  schedules: Schedule[];
  onCancel: () => void;
  onSuccess: (mode: 'add' | 'edit') => void;
}

const ScheduleFormDialogBody = ({ schedule, schedules, onCancel, onSuccess }: ScheduleFormDialogBodyProps) => {
  const queryClient = useQueryClient();
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const isEdit = schedule !== null;

  const { data: departments = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });
  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  });

  const {
    register,
    handleSubmit,
    control,
    watch,
    reset,
    formState: { errors },
  } = useForm<ScheduleFormData>({
    mode: 'onTouched',
    defaultValues: {
      departmentCode: schedule?.department.code ?? '',
      semester: schedule?.semester ?? '',
      courseId: schedule?.courseId ?? '',
      building: schedule?.building ?? '',
      roomNumber: schedule?.roomNumber ?? '',
      days: schedule?.days ?? [],
      startDate: schedule ? schedule.startDate.slice(0, 16) : '',
      endDate: schedule ? schedule.endDate.slice(0, 16) : '',
    },
  });

  const watchedDept = watch('departmentCode');
  const watchedSemester = watch('semester');

  const scheduledCourseIds = new Set(schedules.map((s) => s.courseId));
  const currentCourseId = schedule?.courseId;

  const availableCourses = courses.filter((c) => {
    if (watchedDept && c.departmentCode !== watchedDept) return false;
    if (watchedSemester && c.semester !== watchedSemester) return false;
    if (scheduledCourseIds.has(c.id) && c.id !== currentCourseId) return false;
    return true;
  });

  const { mutate, isPending } = useMutation({
    mutationFn: (payload: SchedulePayload) =>
      isEdit ? updateSchedule(schedule.id, payload) : createSchedule(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] });
      reset({});
      onSuccess(isEdit ? 'edit' : 'add');
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError(isEdit ? 'Failed to update schedule' : 'Failed to add schedule');
      }
    },
  });

  const onSubmit = (data: ScheduleFormData) => {
    setServerErrors(null);
    setFallbackError(null);
    const payload: SchedulePayload = {
      departmentCode: data.departmentCode,
      semester: data.semester as Semester,
      courseId: Number(data.courseId),
      building: data.building as Building,
      roomNumber: data.roomNumber,
      days: data.days,
      startDate: new Date(data.startDate).toISOString(),
      endDate: new Date(data.endDate).toISOString(),
    };
    mutate(payload);
  };

  return (
    <>
      <DialogTitle>{isEdit ? 'Edit schedule' : 'Add schedule'}</DialogTitle>
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
            select
            label='Department'
            fullWidth
            required
            defaultValue={schedule?.department.code ?? ''}
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
            defaultValue={schedule?.semester ?? ''}
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
            label='Course'
            fullWidth
            required
            defaultValue={schedule?.courseId ?? ''}
            error={!!errors.courseId}
            helperText={
              errors.courseId?.message ??
              (!watchedDept || !watchedSemester ? 'Select Department + Semester first' : '')
            }
            {...register('courseId', { required: 'Course is required' })}
          >
            {availableCourses.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.title} — {c.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label='Building'
            fullWidth
            required
            defaultValue={schedule?.building ?? ''}
            error={!!errors.building}
            helperText={errors.building?.message}
            {...register('building', { required: 'Building is required' })}
          >
            {BUILDINGS.map((b) => (
              <MenuItem key={b.value} value={b.value}>
                {b.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label='Room Number'
            fullWidth
            required
            error={!!errors.roomNumber}
            helperText={errors.roomNumber?.message}
            {...register('roomNumber', {
              required: 'Room number is required',
              maxLength: { value: 100, message: 'Max 100 characters' },
            })}
          />
          <Controller
            name='days'
            control={control}
            rules={{ validate: (v) => v.length > 0 || 'Pick at least one day' }}
            render={({ field, fieldState }) => (
              <TextField
                select
                label='Days'
                fullWidth
                required
                error={!!fieldState.error}
                helperText={fieldState.error?.message}
                SelectProps={{
                  multiple: true,
                  value: field.value,
                  onChange: field.onChange,
                  renderValue: (selected) =>
                    (selected as Day[]).map((d) => DAY_SHORT[d]).join(', '),
                }}
              >
                {DAYS.map((d) => (
                  <MenuItem key={d} value={d}>
                    <Checkbox checked={(field.value as Day[]).includes(d)} />
                    <ListItemText primary={d.charAt(0) + d.slice(1).toLowerCase()} />
                  </MenuItem>
                ))}
              </TextField>
            )}
          />
          <TextField
            label='Start (date + class time)'
            type='datetime-local'
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
            error={!!errors.startDate}
            helperText={errors.startDate?.message}
            {...register('startDate', { required: 'Start is required' })}
          />
          <TextField
            label='End (date + class time)'
            type='datetime-local'
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
            error={!!errors.endDate}
            helperText={errors.endDate?.message}
            {...register('endDate', { required: 'End is required' })}
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

function formatDateRange(start: string, end: string): string {
  const s = new Date(start);
  const e = new Date(end);
  const fmt = (d: Date) =>
    `${d.toLocaleDateString()} ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
  return `${fmt(s)} → ${fmt(e)}`;
}

const CourseSchedulePage = () => {
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<Schedule | null>(null);
  const [pendingEdit, setPendingEdit] = useState<Schedule | null>(null);
  const [addOpen, setAddOpen] = useState(false);
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const { data: schedules = [] } = useQuery({
    queryKey: ['schedules'],
    queryFn: fetchSchedules,
  });

  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  });

  const courseTitleById = new Map(courses.map((c) => [c.id, c.title]));

  const { mutate: triggerDelete, isPending: isDeletePending } = useMutation({
    mutationFn: deleteSchedule,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] });
      setPendingDelete(null);
      setSuccessMessage('Schedule deleted');
      setSuccessOpen(true);
    },
    onError: () => {
      setPendingDelete(null);
      setErrorOpen(true);
    },
  });

  const { mutate: triggerToggle, isPending: isTogglePending } = useMutation({
    mutationFn: ({ id, open }: { id: number; open: boolean }) => setScheduleEnrollmentOpen(id, open),
    onSuccess: (_d, vars) => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] });
      setSuccessMessage(vars.open ? 'Enrollment opened' : 'Enrollment closed');
      setSuccessOpen(true);
    },
    onError: () => setErrorOpen(true),
  });

  const onConfirmDelete = () => {
    if (pendingDelete) {
      triggerDelete(pendingDelete.id);
    }
  };

  return (
    <ProtectedPage>
      <ContentWrapper>
        <h1>Schedules</h1>
        <Stack direction='row' justifyContent='flex-end' sx={{ mb: 2 }}>
          <Button variant='contained' onClick={() => setAddOpen(true)}>
            Add Schedule
          </Button>
        </Stack>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Course</TableCell>
                <TableCell>Department</TableCell>
                <TableCell>Semester</TableCell>
                <TableCell>Building</TableCell>
                <TableCell>Room</TableCell>
                <TableCell>Days</TableCell>
                <TableCell>Date Range</TableCell>
                <TableCell>Enrollment</TableCell>
                <TableCell align='right'>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {schedules.map((schedule) => (
                <TableRow key={schedule.id}>
                  <TableCell>{courseTitleById.get(schedule.courseId) ?? `#${schedule.courseId}`}</TableCell>
                  <TableCell>{schedule.department.code}</TableCell>
                  <TableCell>{SEMESTER_LABEL[schedule.semester] ?? schedule.semester}</TableCell>
                  <TableCell>{BUILDING_LABEL[schedule.building] ?? schedule.building}</TableCell>
                  <TableCell>{schedule.roomNumber}</TableCell>
                  <TableCell>{schedule.days.map((d) => DAY_SHORT[d]).join(', ')}</TableCell>
                  <TableCell>{formatDateRange(schedule.startDate, schedule.endDate)}</TableCell>
                  <TableCell>
                    <Button
                      variant='text'
                      size='small'
                      color={schedule.enrollmentOpen ? 'success' : 'warning'}
                      disabled={isTogglePending}
                      onClick={() => triggerToggle({ id: schedule.id, open: !schedule.enrollmentOpen })}
                    >
                      {schedule.enrollmentOpen ? 'Open · Close' : 'Closed · Open'}
                    </Button>
                  </TableCell>
                  <TableCell align='right'>
                    <Button
                      variant='outlined'
                      size='small'
                      sx={{ mr: 1 }}
                      onClick={() => setPendingEdit(schedule)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='contained'
                      color='error'
                      size='small'
                      onClick={() => setPendingDelete(schedule)}
                    >
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        <Dialog open={pendingDelete !== null} onClose={() => setPendingDelete(null)}>
          <DialogTitle>Delete schedule?</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Are you sure you want to delete the schedule for &quot;
              {pendingDelete ? (courseTitleById.get(pendingDelete.courseId) ?? `#${pendingDelete.courseId}`) : ''}
              &quot;? This cannot be undone.
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
            <ScheduleFormDialogBody
              schedule={pendingEdit}
              schedules={schedules}
              onCancel={() => {
                setPendingEdit(null);
                setAddOpen(false);
              }}
              onSuccess={(mode) => {
                setPendingEdit(null);
                setAddOpen(false);
                setSuccessMessage(mode === 'edit' ? 'Schedule updated' : 'Schedule added');
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
          message='Failed to delete schedule'
        />
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default CourseSchedulePage;
