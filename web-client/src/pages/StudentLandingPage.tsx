import axios, { isAxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import {
  Alert,
  Button,
  FormControlLabel,
  MenuItem,
  Paper,
  Snackbar,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useAuth } from '../components/AuthContext';

const COURSE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/courses`;
const SCHEDULE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/schedules`;
const STUDENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/students`;
const ENROLLMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/enrollments`;

type Student = {
  id: number;
  fullName: string;
  email: string;
  registrationNumber: string;
  departmentCode: string;
};

type Course = {
  id: number;
  title: string;
  name: string;
  departmentCode: string;
};

type Schedule = {
  id: number;
  courseId: number;
};

type Enrollment = {
  id: number;
  studentId: number;
  courseScheduleId: number;
  enrollmentDate: string;
  status: 'ON_GOING' | 'PASSED' | 'FAILED' | 'CANCELED';
};

type EnrollmentPayload = {
  studentId: number;
  courseScheduleId: number;
  enrollmentDate: string;
  status: 'ON_GOING';
};

type ServerErrorMessage = { field: string; message: string };

async function fetchSelfStudent(email: string): Promise<Student | null> {
  const response = await axios.get<Student[]>(STUDENT_ENDPOINT);
  return response.data.find((s) => s.email === email) ?? null;
}

async function fetchCourses(): Promise<Course[]> {
  const response = await axios.get<Course[]>(COURSE_ENDPOINT);
  return response.data;
}

async function fetchSchedules(): Promise<Schedule[]> {
  const response = await axios.get<Schedule[]>(SCHEDULE_ENDPOINT);
  return response.data;
}

async function fetchEnrollments(): Promise<Enrollment[]> {
  const response = await axios.get<Enrollment[]>(ENROLLMENT_ENDPOINT);
  return response.data;
}

async function createEnrollment(payload: EnrollmentPayload): Promise<void> {
  await axios.post(ENROLLMENT_ENDPOINT, payload);
}

const StudentLandingPage = () => {
  const queryClient = useQueryClient();
  const { email } = useAuth();

  const [selectedCourseId, setSelectedCourseId] = useState<number | ''>('');
  const [showAll, setShowAll] = useState(false);
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const [successOpen, setSuccessOpen] = useState(false);

  const { data: self = null } = useQuery({
    queryKey: ['self-student', email],
    queryFn: () => fetchSelfStudent(email as string),
    enabled: !!email,
  });
  const { data: courses = [] } = useQuery({ queryKey: ['courses'], queryFn: fetchCourses });
  const { data: schedules = [] } = useQuery({ queryKey: ['schedules'], queryFn: fetchSchedules });
  const { data: enrollments = [] } = useQuery({ queryKey: ['enrollments'], queryFn: fetchEnrollments });

  const scheduleByCourseId = new Map(schedules.map((s) => [s.courseId, s]));
  const courseById = new Map(courses.map((c) => [c.id, c]));

  const myEnrollments = self ? enrollments.filter((e) => e.studentId === self.id) : [];
  const myEnrolledScheduleIds = new Set(myEnrollments.map((e) => e.courseScheduleId));
  const myEnrolledCourseIds = new Set(
    schedules.filter((s) => myEnrolledScheduleIds.has(s.id)).map((s) => s.courseId),
  );

  const availableCourses = self ? courses.filter((c) => {
    if (!scheduleByCourseId.has(c.id)) return false;
    if (myEnrolledCourseIds.has(c.id)) return false;
    if (!showAll && c.departmentCode !== self.departmentCode) return false;
    return true;
  }) : [];

  const { mutate, isPending } = useMutation({
    mutationFn: createEnrollment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['enrollments'] });
      setSelectedCourseId('');
      setSuccessOpen(true);
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError('Failed to enroll');
      }
    },
  });

  const onEnroll = () => {
    setServerErrors(null);
    setFallbackError(null);
    if (!self || selectedCourseId === '') return;
    const schedule = scheduleByCourseId.get(Number(selectedCourseId));
    if (!schedule) {
      setFallbackError('Selected course has no schedule');
      return;
    }
    const payload: EnrollmentPayload = {
      studentId: self.id,
      courseScheduleId: schedule.id,
      enrollmentDate: new Date().toISOString(),
      status: 'ON_GOING',
    };
    mutate(payload);
  };

  if (!self) {
    return (
      <>
        <Typography variant='h5' sx={{ mt: 2 }}>Welcome Student</Typography>
        <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
          Student record not found for {email ?? 'this account'}.
        </Typography>
      </>
    );
  }

  return (
    <>
      <Typography variant='h5' sx={{ mt: 2 }}>Welcome, {self.fullName}</Typography>

      <Paper sx={{ p: 2, mt: 3 }}>
        <Stack spacing={1}>
          <Typography variant='body1'><strong>Reg No:</strong> {self.registrationNumber}</Typography>
          <Typography variant='body1'><strong>Name:</strong> {self.fullName}</Typography>
          <Typography variant='body1'><strong>Email:</strong> {self.email}</Typography>
          <Typography variant='body1'><strong>Department:</strong> {self.departmentCode}</Typography>
          <Typography variant='body1'><strong>Date:</strong> {new Date().toLocaleDateString()}</Typography>
        </Stack>
      </Paper>

      <Typography variant='h6' sx={{ mt: 4, mb: 1 }}>Enroll in a Course</Typography>
      {serverErrors && serverErrors.length > 0 && (
        <Alert severity='error' sx={{ mb: 2, whiteSpace: 'pre-line' }}>
          {serverErrors.map((e) => (e.field === '*' ? e.message : `${e.field}: ${e.message}`)).join('\n')}
        </Alert>
      )}
      {fallbackError && (
        <Alert severity='error' sx={{ mb: 2 }}>{fallbackError}</Alert>
      )}
      <Stack direction='row' spacing={2} alignItems='center' sx={{ mb: 2 }}>
        <TextField
          select
          label='Course to enroll'
          size='small'
          sx={{ minWidth: 320 }}
          value={selectedCourseId}
          onChange={(e) => setSelectedCourseId(e.target.value === '' ? '' : Number(e.target.value))}
        >
          {availableCourses.length === 0 && (
            <MenuItem value='' disabled>No courses available</MenuItem>
          )}
          {availableCourses.map((c) => (
            <MenuItem key={c.id} value={c.id}>{c.title} — {c.name}</MenuItem>
          ))}
        </TextField>
        <Button variant='contained' onClick={onEnroll} disabled={selectedCourseId === '' || isPending}>
          Enroll
        </Button>
        <FormControlLabel
          control={<Switch checked={showAll} onChange={(e) => setShowAll(e.target.checked)} />}
          label='Show all courses of University'
        />
      </Stack>

      <Typography variant='h6' sx={{ mt: 4, mb: 1 }}>Enrolled Courses</Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Sl</TableCell>
              <TableCell>Title</TableCell>
              <TableCell>Name</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {myEnrollments.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3}>No enrollments yet.</TableCell>
              </TableRow>
            ) : myEnrollments.map((e, idx) => {
              const schedule = schedules.find((s) => s.id === e.courseScheduleId);
              const course = schedule ? courseById.get(schedule.courseId) : null;
              return (
                <TableRow key={e.id}>
                  <TableCell>{idx + 1}</TableCell>
                  <TableCell>{course?.title ?? `#${e.courseScheduleId}`}</TableCell>
                  <TableCell>{course?.name ?? ''}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        message='Enrolled successfully'
      />
    </>
  );
};

export default StudentLandingPage;
