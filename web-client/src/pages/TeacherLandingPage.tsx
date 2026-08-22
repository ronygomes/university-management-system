import axios from 'axios';
import { useQuery } from '@tanstack/react-query';
import {
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
const COURSE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/courses`;
const SCHEDULE_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/schedules`;
const ME_TEACHER_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/me/teacher`;

const ALL_ROWS_SIZE = 1000;

type PagedResponse<T> = { content: T[] };

type Semester =
  | 'FIRST_YEAR_FIRST' | 'FIRST_YEAR_SECOND'
  | 'SECOND_YEAR_FIRST' | 'SECOND_YEAR_SECOND'
  | 'THIRD_YEAR_FIRST' | 'THIRD_YEAR_SECOND'
  | 'FOURTH_YEAR_FIRST' | 'FOURTH_YEAR_SECOND'
  | 'FIFTH_YEAR_FIRST' | 'FIFTH_YEAR_SECOND';

type Building = 'BUILDING_1' | 'BUILDING_2';
type Day = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

const SEMESTER_LABEL: Record<Semester, string> = {
  FIRST_YEAR_FIRST: '1st Year - 1st Semester',
  FIRST_YEAR_SECOND: '1st Year - 2nd Semester',
  SECOND_YEAR_FIRST: '2nd Year - 1st Semester',
  SECOND_YEAR_SECOND: '2nd Year - 2nd Semester',
  THIRD_YEAR_FIRST: '3rd Year - 1st Semester',
  THIRD_YEAR_SECOND: '3rd Year - 2nd Semester',
  FOURTH_YEAR_FIRST: '4th Year - 1st Semester',
  FOURTH_YEAR_SECOND: '4th Year - 2nd Semester',
  FIFTH_YEAR_FIRST: '5th Year - 1st Semester',
  FIFTH_YEAR_SECOND: '5th Year - 2nd Semester',
};

const BUILDING_LABEL: Record<Building, string> = {
  BUILDING_1: 'Building 1',
  BUILDING_2: 'Building 2',
};

const DAY_SHORT: Record<Day, string> = {
  MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu',
  FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun',
};

type Course = {
  id: number;
  code: string;
  name: string;
  credit: number;
  semester: Semester;
  departmentCode: string;
  instructorIds: number[];
};

type Schedule = {
  id: number;
  courseId: number;
  building: Building;
  roomNumber: string;
  days: Day[];
  startDate: string;
  endDate: string;
};

type TeacherSelf = { id: number; fullName: string } | null;

async function fetchSelfTeacher(): Promise<TeacherSelf> {
  const response = await axios.get<{ id: number; fullName: string }>(ME_TEACHER_ENDPOINT);
  return response.data;
}

async function fetchCourses(): Promise<Course[]> {
  const response = await axios.get<PagedResponse<Course>>(COURSE_ENDPOINT, {
    params: { size: ALL_ROWS_SIZE },
  });
  return response.data.content;
}

async function fetchSchedules(): Promise<Schedule[]> {
  const response = await axios.get<PagedResponse<Schedule>>(SCHEDULE_ENDPOINT, {
    params: { size: ALL_ROWS_SIZE },
  });
  return response.data.content;
}

const TeacherLandingPage = () => {
  const { data: teacherSelf } = useQuery({
    queryKey: ['me-teacher'],
    queryFn: fetchSelfTeacher,
  });

  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  });

  const { data: schedules = [] } = useQuery({
    queryKey: ['schedules'],
    queryFn: fetchSchedules,
  });

  const teacherId = teacherSelf?.id ?? null;
  const myCourses = teacherId !== null
    ? courses.filter((c) => c.instructorIds.includes(teacherId))
    : [];
  const myCourseIds = new Set(myCourses.map((c) => c.id));
  const mySchedules = schedules.filter((s) => myCourseIds.has(s.courseId));
  const courseCodeById = new Map(courses.map((c) => [c.id, c.code]));

  return (
    <>
      <Typography variant='h5' sx={{ mt: 2 }}>
        Welcome{teacherSelf ? `, ${teacherSelf.fullName}` : ' Teacher'}
      </Typography>

      <Stack spacing={4} sx={{ mt: 3 }}>
        <section>
          <Typography variant='h6' sx={{ mb: 1 }}>My Assigned Courses</Typography>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Code</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Credit</TableCell>
                  <TableCell>Semester</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {myCourses.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4}>No assigned courses yet.</TableCell>
                  </TableRow>
                ) : myCourses.map((c) => (
                  <TableRow key={c.id}>
                    <TableCell>{c.code}</TableCell>
                    <TableCell>{c.name}</TableCell>
                    <TableCell>{c.credit}</TableCell>
                    <TableCell>{SEMESTER_LABEL[c.semester] ?? c.semester}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </section>

        <section>
          <Typography variant='h6' sx={{ mb: 1 }}>My Schedule</Typography>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Course</TableCell>
                  <TableCell>Building</TableCell>
                  <TableCell>Room</TableCell>
                  <TableCell>Days</TableCell>
                  <TableCell>Start</TableCell>
                  <TableCell>End</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {mySchedules.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6}>No schedule yet.</TableCell>
                  </TableRow>
                ) : mySchedules.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell>{courseCodeById.get(s.courseId) ?? `#${s.courseId}`}</TableCell>
                    <TableCell>{BUILDING_LABEL[s.building] ?? s.building}</TableCell>
                    <TableCell>{s.roomNumber}</TableCell>
                    <TableCell>{s.days.map((d) => DAY_SHORT[d]).join(', ')}</TableCell>
                    <TableCell>{s.startDate}</TableCell>
                    <TableCell>{s.endDate}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </section>
      </Stack>
    </>
  );
};

export default TeacherLandingPage;
