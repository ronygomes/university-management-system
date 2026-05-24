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
const ENROLLMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/enrollments`;
const ME_STUDENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/me/student`;
const ME_ENROLLMENTS_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/me/enrollments`;

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
  credit: number;
  departmentCode: string;
};

type GradeLetter =
  | 'A_PLUS' | 'A' | 'A_MINUS'
  | 'B_PLUS' | 'B' | 'B_MINUS'
  | 'C_PLUS' | 'C' | 'C_MINUS'
  | 'F';

const GRADE_POINT: Record<GradeLetter, number> = {
  A_PLUS: 4.0, A: 3.75, A_MINUS: 3.5,
  B_PLUS: 3.25, B: 3.0, B_MINUS: 2.75,
  C_PLUS: 2.5, C: 2.25, C_MINUS: 2.0,
  F: 0.0,
};

const GRADE_LABEL: Record<GradeLetter, string> = {
  A_PLUS: 'A+', A: 'A', A_MINUS: 'A-',
  B_PLUS: 'B+', B: 'B', B_MINUS: 'B-',
  C_PLUS: 'C+', C: 'C', C_MINUS: 'C-',
  F: 'F',
};

function letterFromCgpa(cgpa: number): GradeLetter {
  if (cgpa >= 3.875) return 'A_PLUS';
  if (cgpa >= 3.625) return 'A';
  if (cgpa >= 3.375) return 'A_MINUS';
  if (cgpa >= 3.125) return 'B_PLUS';
  if (cgpa >= 2.875) return 'B';
  if (cgpa >= 2.625) return 'B_MINUS';
  if (cgpa >= 2.375) return 'C_PLUS';
  if (cgpa >= 2.125) return 'C';
  if (cgpa >= 1.0) return 'C_MINUS';
  return 'F';
}

type Schedule = {
  id: number;
  courseId: number;
  enrollmentOpen: boolean;
};

type Enrollment = {
  id: number;
  studentId: number;
  courseScheduleId: number;
  enrollmentDate: string;
  status: 'ON_GOING' | 'PASSED' | 'FAILED' | 'CANCELED';
  grade: GradeLetter | null;
};

type EnrollmentPayload = {
  studentId: number;
  courseScheduleId: number;
  enrollmentDate: string;
  status: 'ON_GOING';
};

type ServerErrorMessage = { field: string; message: string };

async function fetchSelfStudent(): Promise<Student | null> {
  const response = await axios.get<Student>(ME_STUDENT_ENDPOINT);
  return response.data;
}

async function fetchCourses(): Promise<Course[]> {
  const response = await axios.get<Course[]>(COURSE_ENDPOINT);
  return response.data;
}

async function fetchSchedules(): Promise<Schedule[]> {
  const response = await axios.get<Schedule[]>(SCHEDULE_ENDPOINT);
  return response.data;
}

async function fetchMyEnrollments(): Promise<Enrollment[]> {
  const response = await axios.get<Enrollment[]>(ME_ENROLLMENTS_ENDPOINT);
  return response.data;
}

async function createEnrollment(payload: EnrollmentPayload): Promise<void> {
  await axios.post(ENROLLMENT_ENDPOINT, payload);
}

interface ResultSummaryProps {
  myEnrollments: Enrollment[];
  scheduleByCourseId: Map<number, Schedule>;
  courseById: Map<number, Course>;
}

const ResultSummary = ({ myEnrollments, scheduleByCourseId, courseById }: ResultSummaryProps) => {
  const scheduleById = new Map<number, Schedule>();
  scheduleByCourseId.forEach((s) => scheduleById.set(s.id, s));

  const rows = myEnrollments
    .filter((e) => e.status !== 'CANCELED')
    .map((e) => {
      const schedule = scheduleById.get(e.courseScheduleId);
      const course = schedule ? courseById.get(schedule.courseId) : undefined;
      return { enrollment: e, course };
    });

  const enrolledCount = rows.length;
  const completed = rows.filter((r) => r.enrollment.grade !== null);
  const remaining = rows.filter((r) => r.enrollment.grade === null);

  const totalCredit = rows.reduce((sum, r) => sum + (r.course?.credit ?? 0), 0);
  const completedCredit = completed.reduce((sum, r) => sum + (r.course?.credit ?? 0), 0);
  const remainingCredit = remaining.reduce((sum, r) => sum + (r.course?.credit ?? 0), 0);

  let cgpa: number | null = null;
  let cgpaLetter: GradeLetter | null = null;
  if (completedCredit > 0) {
    const points = completed.reduce(
      (sum, r) => sum + (GRADE_POINT[r.enrollment.grade as GradeLetter] * (r.course?.credit ?? 0)),
      0,
    );
    cgpa = Math.round((points / completedCredit) * 100) / 100;
    cgpaLetter = letterFromCgpa(cgpa);
  }

  const eligible = enrolledCount > 0 && rows.every((r) => r.enrollment.status === 'PASSED');

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant='body2' sx={{ color: 'text.secondary', mb: 1 }}>
        Result as of {new Date().toLocaleString()}
      </Typography>
      <Stack spacing={0.5} sx={{ mb: 2 }}>
        <Typography>No of enrolled courses: <strong>{enrolledCount}</strong></Typography>
        <Typography>No of completed courses: <strong>{completed.length}</strong></Typography>
        <Typography>No of remaining courses: <strong>{remaining.length}</strong></Typography>
        <Typography>Total credit of enrolled courses: <strong>{totalCredit}</strong></Typography>
        <Typography>Completed credit: <strong>{completedCredit}</strong></Typography>
        <Typography>Remaining credit: <strong>{remainingCredit}</strong></Typography>
        <Typography>
          CGPA: <strong>{cgpa === null ? 'N/A' : cgpa.toFixed(2)}</strong>
          {cgpaLetter && <> &nbsp; Grade Letter: <strong>{GRADE_LABEL[cgpaLetter]}</strong></>}
        </Typography>
        {eligible && (
          <Typography sx={{ color: 'success.main', mt: 1 }}>
            You are eligible to get the certificate.
          </Typography>
        )}
      </Stack>

      <TableContainer component={Paper} variant='outlined'>
        <Table size='small'>
          <TableHead>
            <TableRow>
              <TableCell>Sl</TableCell>
              <TableCell>Course Name</TableCell>
              <TableCell>Credit</TableCell>
              <TableCell>Status / Grade</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.length === 0 ? (
              <TableRow><TableCell colSpan={4}>No enrollments yet.</TableCell></TableRow>
            ) : rows.map((r, idx) => (
              <TableRow key={r.enrollment.id}>
                <TableCell>{idx + 1}</TableCell>
                <TableCell>{r.course?.name ?? `#${r.enrollment.courseScheduleId}`}</TableCell>
                <TableCell>{r.course?.credit ?? ''}</TableCell>
                <TableCell>
                  {r.enrollment.grade
                    ? GRADE_LABEL[r.enrollment.grade]
                    : r.enrollment.status}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
};

const StudentLandingPage = () => {
  const queryClient = useQueryClient();
  const { email } = useAuth();

  const [selectedCourseId, setSelectedCourseId] = useState<number | ''>('');
  const [showAll, setShowAll] = useState(false);
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const [successOpen, setSuccessOpen] = useState(false);

  const { data: self = null } = useQuery({
    queryKey: ['me-student'],
    queryFn: fetchSelfStudent,
  });
  const { data: courses = [] } = useQuery({ queryKey: ['courses'], queryFn: fetchCourses });
  const { data: schedules = [] } = useQuery({ queryKey: ['schedules'], queryFn: fetchSchedules });
  const { data: enrollments = [] } = useQuery({ queryKey: ['me-enrollments'], queryFn: fetchMyEnrollments });

  const scheduleByCourseId = new Map(schedules.map((s) => [s.courseId, s]));
  const courseById = new Map(courses.map((c) => [c.id, c]));

  const myEnrollments = self ? enrollments.filter((e) => e.studentId === self.id) : [];
  const myEnrolledScheduleIds = new Set(myEnrollments.map((e) => e.courseScheduleId));
  const myEnrolledCourseIds = new Set(
    schedules.filter((s) => myEnrolledScheduleIds.has(s.id)).map((s) => s.courseId),
  );

  const availableCourses = self ? courses.filter((c) => {
    const sched = scheduleByCourseId.get(c.id);
    if (!sched) return false;
    if (!sched.enrollmentOpen) return false;
    if (myEnrolledCourseIds.has(c.id)) return false;
    if (!showAll && c.departmentCode !== self.departmentCode) return false;
    return true;
  }) : [];

  const { mutate, isPending } = useMutation({
    mutationFn: createEnrollment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['me-enrollments'] });
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

      <Typography variant='h6' sx={{ mt: 4, mb: 1 }}>Academic Result</Typography>
      <ResultSummary
        myEnrollments={myEnrollments}
        scheduleByCourseId={scheduleByCourseId}
        courseById={courseById}
      />

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
