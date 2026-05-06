import {
  Box,
  Button,
  Container,
  Grid,
  InputAdornment,
  MenuItem,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useQuery, useMutation } from '@tanstack/react-query';
import axios from 'axios';
import { useForm } from 'react-hook-form';
import { useRef, useState } from 'react';
import ContentWrapper from '../components/ContentWrapper';

const DEPARTMENTS_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;
const STUDENTS_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/students`;

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const CONTACT_NUMBER_SUFFIX_REGEX = /^\d{10}$/;
const BANGLADESH_PHONE_CODE = '+880';

const EXAM_TYPES = [
  { value: 'SSC', label: 'Secondary School Certificate' },
  { value: 'HSC', label: 'Higher Secondary Certificate' },
  { value: 'A_LEVEL', label: 'A Level' },
  { value: 'O_LEVEL', label: 'O Level' },
];

const GRADES = [
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

type Department = {
  code: string;
  name: string;
};

type StudentFormData = {
  fullName: string;
  email: string;
  departmentCode: string;
  contactNumberSuffix: string;
  address: string;
};

type CreateStudentPayload = Omit<StudentFormData, 'contactNumberSuffix'> & {
  contactNumber: string;
  educations: Education[];
};

type EducationFormData = {
  examType: string;
  grade: string;
  cgpa: number;
  certificateFile: FileList;
};

export type Education = {
  examType: string;
  grade: string;
  cgpa: number;
  certificateFileName: string;
  certificatePath: string;
};

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENTS_ENDPOINT);
  return response.data._embedded.departments;
}

async function createStudent(data: CreateStudentPayload): Promise<void> {
  await axios.post(STUDENTS_ENDPOINT, data);
}

interface EducationSectionProps {
  onAdd: (education: Education) => void;
}

const EducationSection = ({ onAdd }: EducationSectionProps) => {
  const [educations, setEducations] = useState<Education[]>([]);
  const [formKey, setFormKey] = useState(0);

  const {
    register,
    trigger,
    getValues,
    reset,
    formState: { errors },
  } = useForm<EducationFormData>();

  const onClickAddToList = async () => {
    const isValid = await trigger();
    if (!isValid) return;

    const data = getValues();
    const fileName = data.certificateFile?.[0]?.name ?? 'Sample Filename';
    const education: Education = {
      examType: data.examType,
      grade: data.grade,
      cgpa: data.cgpa,
      certificateFileName: fileName,
      certificatePath: 'Byte',
    };
    setEducations((prev) => [...prev, education]);
    onAdd(education);
    reset({});
    setFormKey((k) => k + 1);
  };

  return (
    <Box
      sx={{
        position: 'relative',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 1,
        p: 2,
        mt: 1,
      }}
    >
      <Typography
        variant='caption'
        sx={{
          position: 'absolute',
          top: -10,
          left: 12,
          px: 0.5,
          bgcolor: 'background.paper',
        }}
      >
        Education
      </Typography>

      <Grid key={formKey} container spacing={2}>
        <Grid size={6}>
          <TextField
            select
            label='Exam'
            fullWidth
            required
            defaultValue=''
            error={!!errors.examType}
            helperText={errors.examType?.message}
            {...register('examType', { required: 'Exam is required' })}
          >
            {EXAM_TYPES.map((exam) => (
              <MenuItem key={exam.value} value={exam.value}>
                {exam.label}
              </MenuItem>
            ))}
          </TextField>
        </Grid>

        <Grid size={6}>
          <TextField
            select
            label='Grade'
            fullWidth
            required
            defaultValue=''
            error={!!errors.grade}
            helperText={errors.grade?.message}
            {...register('grade', { required: 'Grade is required' })}
          >
            {GRADES.map((g) => (
              <MenuItem key={g.value} value={g.value}>
                {g.label}
              </MenuItem>
            ))}
          </TextField>
        </Grid>

        <Grid size={6}>
          <TextField
            label='CGPA'
            fullWidth
            required
            type='number'
            inputProps={{ min: 0, max: 5, step: 0.01 }}
            error={!!errors.cgpa}
            helperText={errors.cgpa?.message}
            {...register('cgpa', {
              required: 'CGPA is required',
              min: { value: 0, message: 'Min 0.0' },
              max: { value: 5, message: 'Max 5.0' },
            })}
          />
        </Grid>

        <Grid size={6}>
          <TextField
            label='Certificate'
            fullWidth
            type='file'
            slotProps={{ inputLabel: { shrink: true } }}
            {...register('certificateFile')}
          />
        </Grid>

        <Grid size={12} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button type='button' variant='outlined' onClick={onClickAddToList}>
            Add to List
          </Button>
        </Grid>
      </Grid>

      {educations.length > 0 && (
        <Table size='small' sx={{ mt: 2 }}>
          <TableHead>
            <TableRow>
              <TableCell>Exam</TableCell>
              <TableCell>Grade</TableCell>
              <TableCell>CGPA</TableCell>
              <TableCell>Certificate</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {educations.map((edu, index) => (
              <TableRow key={index}>
                <TableCell>
                  {EXAM_TYPES.find((e) => e.value === edu.examType)?.label}
                </TableCell>
                <TableCell>
                  {GRADES.find((g) => g.value === edu.grade)?.label}
                </TableCell>
                <TableCell>{edu.cgpa}</TableCell>
                <TableCell>{edu.certificateFileName}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Box>
  );
};

const StudentRegistrationPage = () => {
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [educationKey, setEducationKey] = useState(0);
  const educationsRef = useRef<Education[]>([]);

  const { data: departments = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });

  const { mutate, isPending } = useMutation({
    mutationFn: createStudent,
    onSuccess: () => {
      reset();
      educationsRef.current = [];
      setEducationKey((k) => k + 1);
      setSuccessOpen(true);
    },
    onError: () => {
      setErrorOpen(true);
    },
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<StudentFormData>();

  const onSubmit = (data: StudentFormData) => {
    const { contactNumberSuffix, ...rest } = data;
    const payload: CreateStudentPayload = {
      ...rest,
      contactNumber: contactNumberSuffix ? `${BANGLADESH_PHONE_CODE}${contactNumberSuffix}` : '',
      educations: educationsRef.current,
    };
    console.log('Submitting student:', payload);
    mutate(payload);
  };

  return (
    <ContentWrapper>
      <Container maxWidth='md'>
        <Paper elevation={3} sx={{ mt: 4, p: 4 }}>
          <Typography variant='h5' sx={{ mb: 3 }}>
            Student Registration
          </Typography>

          <Box component='form' onSubmit={handleSubmit(onSubmit)} noValidate>
            <Grid container spacing={2}>
              <Grid size={12}>
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
              </Grid>

              <Grid size={6}>
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
              </Grid>

              <Grid size={6}>
                <TextField
                  select
                  label='Department'
                  fullWidth
                  required
                  defaultValue=''
                  error={!!errors.departmentCode}
                  helperText={errors.departmentCode?.message}
                  {...register('departmentCode', {
                    required: 'Department is required',
                  })}
                >
                  {departments.map((dept) => (
                    <MenuItem key={dept.code} value={dept.code}>
                      {dept.name}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              <Grid size={6}>
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
              </Grid>

              <Grid size={12}>
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
              </Grid>

              <Grid size={12}>
                <EducationSection
                  key={educationKey}
                  onAdd={(edu) => { educationsRef.current = [...educationsRef.current, edu]; }}
                />
              </Grid>

              <Grid size={12}>
                <Button type='submit' variant='contained' fullWidth disabled={isPending}>
                  {isPending ? 'Registering...' : 'Register'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </Paper>

        <Snackbar
          open={successOpen}
          autoHideDuration={4000}
          onClose={() => setSuccessOpen(false)}
          message='Student registered successfully'
        />
        <Snackbar
          open={errorOpen}
          autoHideDuration={4000}
          onClose={() => setErrorOpen(false)}
          message='Registration failed. Please try again.'
        />
      </Container>
    </ContentWrapper>
  );
};

export default StudentRegistrationPage;
