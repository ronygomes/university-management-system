import axios, { isAxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import {
  Alert,
  Button,
  MenuItem,
  Paper,
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

const COURSE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/courses`;
const SCHEDULE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/schedules`;
const STUDENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/students`;
const ENROLLMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/enrollments`;
const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;

type GradeLetter =
  | 'A_PLUS' | 'A' | 'A_MINUS'
  | 'B_PLUS' | 'B' | 'B_MINUS'
  | 'C_PLUS' | 'C' | 'C_MINUS'
  | 'F';

const GRADES: { value: GradeLetter; label: string }[] = [
  { value: 'A_PLUS', label: 'A+' },
  { value: 'A', label: 'A' },
  { value: 'A_MINUS', label: 'A-' },
  { value: 'B_PLUS', label: 'B+' },
  { value: 'B', label: 'B' },
  { value: 'B_MINUS', label: 'B-' },
  { value: 'C_PLUS', label: 'C+' },
  { value: 'C', label: 'C' },
  { value: 'C_MINUS', label: 'C-' },
  { value: 'F', label: 'F' },
];

type Department = { code: string; name: string };
type Course = { id: number; title: string; name: string; departmentCode: string };
type Schedule = { id: number; courseId: number };
type Student = { id: number; fullName: string; email: string; registrationNumber: string };
type Enrollment = {
  id: number;
  studentId: number;
  courseScheduleId: number;
  enrollmentDate: string;
  status: 'ON_GOING' | 'PASSED' | 'FAILED' | 'CANCELED';
  grade: GradeLetter | null;
};

type ServerErrorMessage = { field: string; message: string };

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENT_ENDPOINT);
  return response.data._embedded?.departments ?? [];
}
async function fetchCourses(): Promise<Course[]> {
  return (await axios.get<Course[]>(COURSE_ENDPOINT)).data;
}
async function fetchSchedules(): Promise<Schedule[]> {
  return (await axios.get<Schedule[]>(SCHEDULE_ENDPOINT)).data;
}
async function fetchStudents(): Promise<Student[]> {
  return (await axios.get<Student[]>(STUDENT_ENDPOINT)).data;
}
async function fetchEnrollments(): Promise<Enrollment[]> {
  return (await axios.get<Enrollment[]>(ENROLLMENT_ENDPOINT)).data;
}

async function patchEnrollment(id: number, payload: { grade: GradeLetter; status: 'PASSED' | 'FAILED' }): Promise<void> {
  await axios.patch(`${ENROLLMENT_ENDPOINT}/${id}`, payload);
}

const ResultEntryPage = () => {
  const queryClient = useQueryClient();
  const [departmentFilter, setDepartmentFilter] = useState<string>('');
  const [courseFilter, setCourseFilter] = useState<number | ''>('');
  const [draftGrades, setDraftGrades] = useState<Record<number, GradeLetter | ''>>({});
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const [successOpen, setSuccessOpen] = useState(false);

  const { data: departments = [] } = useQuery({ queryKey: ['departments'], queryFn: fetchDepartments });
  const { data: courses = [] } = useQuery({ queryKey: ['courses'], queryFn: fetchCourses });
  const { data: schedules = [] } = useQuery({ queryKey: ['schedules'], queryFn: fetchSchedules });
  const { data: students = [] } = useQuery({ queryKey: ['students'], queryFn: fetchStudents });
  const { data: enrollments = [] } = useQuery({ queryKey: ['enrollments'], queryFn: fetchEnrollments });

  const studentById = new Map(students.map((s) => [s.id, s]));
  const scheduleByCourseId = new Map(schedules.map((s) => [s.courseId, s]));

  const filteredCourses = departmentFilter
    ? courses.filter((c) => c.departmentCode === departmentFilter && scheduleByCourseId.has(c.id))
    : [];

  const selectedSchedule = courseFilter ? scheduleByCourseId.get(Number(courseFilter)) : undefined;
  const roster = selectedSchedule
    ? enrollments.filter((e) => e.courseScheduleId === selectedSchedule.id)
    : [];

  const { mutate, isPending } = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: { grade: GradeLetter; status: 'PASSED' | 'FAILED' } }) =>
      patchEnrollment(id, payload),
    onSuccess: (_d, vars) => {
      queryClient.invalidateQueries({ queryKey: ['enrollments'] });
      setDraftGrades((d) => {
        const next = { ...d };
        delete next[vars.id];
        return next;
      });
      setSuccessOpen(true);
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError('Failed to save grade');
      }
    },
  });

  const onSaveRow = (enrollment: Enrollment) => {
    setServerErrors(null);
    setFallbackError(null);
    const grade = draftGrades[enrollment.id] ?? enrollment.grade ?? '';
    if (grade === '') return;
    const status: 'PASSED' | 'FAILED' = grade === 'F' ? 'FAILED' : 'PASSED';
    mutate({ id: enrollment.id, payload: { grade, status } });
  };

  const currentGradeFor = (e: Enrollment): GradeLetter | '' =>
    e.id in draftGrades ? draftGrades[e.id] : (e.grade ?? '');

  const isDirty = (e: Enrollment): boolean => {
    const draft = draftGrades[e.id];
    if (draft === undefined) return false;
    return draft !== (e.grade ?? '') && draft !== '';
  };

  return (
    <ProtectedPage>
      <ContentWrapper>
        <h1>Result Entry</h1>
        {serverErrors && serverErrors.length > 0 && (
          <Alert severity='error' sx={{ mb: 2, whiteSpace: 'pre-line' }}>
            {serverErrors.map((e) => (e.field === '*' ? e.message : `${e.field}: ${e.message}`)).join('\n')}
          </Alert>
        )}
        {fallbackError && (
          <Alert severity='error' sx={{ mb: 2 }}>{fallbackError}</Alert>
        )}
        <Stack direction='row' spacing={2} sx={{ mb: 2 }}>
          <TextField
            select
            label='Department'
            size='small'
            sx={{ minWidth: 220 }}
            value={departmentFilter}
            onChange={(e) => {
              setDepartmentFilter(e.target.value);
              setCourseFilter('');
              setDraftGrades({});
            }}
          >
            <MenuItem value=''>Select</MenuItem>
            {departments.map((d) => (
              <MenuItem key={d.code} value={d.code}>{d.name}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label='Course'
            size='small'
            sx={{ minWidth: 320 }}
            value={courseFilter}
            onChange={(e) => {
              setCourseFilter(e.target.value === '' ? '' : Number(e.target.value));
              setDraftGrades({});
            }}
            disabled={!departmentFilter}
          >
            <MenuItem value=''>Select</MenuItem>
            {filteredCourses.map((c) => (
              <MenuItem key={c.id} value={c.id}>{c.title} — {c.name}</MenuItem>
            ))}
          </TextField>
        </Stack>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Reg No</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Grade</TableCell>
                <TableCell align='right'>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {!selectedSchedule ? (
                <TableRow>
                  <TableCell colSpan={5}>Select a department and course to load the roster.</TableCell>
                </TableRow>
              ) : roster.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5}>No students enrolled in this course.</TableCell>
                </TableRow>
              ) : roster.map((e) => {
                const student = studentById.get(e.studentId);
                const value = currentGradeFor(e);
                return (
                  <TableRow key={e.id}>
                    <TableCell>{student?.registrationNumber ?? `#${e.studentId}`}</TableCell>
                    <TableCell>{student?.fullName ?? ''}</TableCell>
                    <TableCell>{student?.email ?? ''}</TableCell>
                    <TableCell>
                      <TextField
                        select
                        size='small'
                        sx={{ minWidth: 100 }}
                        value={value}
                        onChange={(ev) => setDraftGrades((d) => ({ ...d, [e.id]: ev.target.value as GradeLetter | '' }))}
                      >
                        <MenuItem value=''>—</MenuItem>
                        {GRADES.map((g) => (
                          <MenuItem key={g.value} value={g.value}>{g.label}</MenuItem>
                        ))}
                      </TextField>
                    </TableCell>
                    <TableCell align='right'>
                      <Button
                        variant='contained'
                        size='small'
                        onClick={() => onSaveRow(e)}
                        disabled={!isDirty(e) || isPending}
                      >
                        Save
                      </Button>
                    </TableCell>
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
          message='Grade saved'
        />
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default ResultEntryPage;
