import axios, { isAxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
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

const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;

type Department = {
  code: string;
  name: string;
};

type DepartmentFormData = {
  code: string;
  name: string;
};

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENT_ENDPOINT);
  return response.data._embedded.departments;
}

async function deleteDepartment(code: string): Promise<void> {
  await axios.delete(`${DEPARTMENT_ENDPOINT}/${code}`);
}

async function updateDepartment(originalCode: string, data: DepartmentFormData): Promise<void> {
  await axios.put(`${DEPARTMENT_ENDPOINT}/${originalCode}`, data);
}

async function createDepartment(data: DepartmentFormData): Promise<void> {
  await axios.post(DEPARTMENT_ENDPOINT, data);
}

interface DepartmentFormDialogBodyProps {
  department: Department | null;
  onSuccess: (mode: 'add' | 'edit') => void;
  onCancel: () => void;
}

type ServerErrorMessage = { field: string; message: string };

const DepartmentFormDialogBody = ({ department, onSuccess, onCancel }: DepartmentFormDialogBodyProps) => {
  const queryClient = useQueryClient();
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const isEdit = department !== null;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DepartmentFormData>({
    defaultValues: { code: department?.code ?? '', name: department?.name ?? '' },
  });

  const { mutate, isPending } = useMutation({
    mutationFn: (data: DepartmentFormData) =>
      isEdit ? updateDepartment(department.code, data) : createDepartment(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
      reset({});
      onSuccess(isEdit ? 'edit' : 'add');
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError(isEdit ? 'Failed to update department' : 'Failed to add department');
      }
    },
  });

  const onSubmit = (data: DepartmentFormData) => {
    setServerErrors(null);
    setFallbackError(null);
    mutate(data);
  };

  return (
    <>
      <DialogTitle>{isEdit ? 'Edit department' : 'Add department'}</DialogTitle>
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
            label='Code'
            fullWidth
            required
            error={!!errors.code}
            helperText={errors.code?.message}
            {...register('code', {
              required: 'Code is required',
              maxLength: { value: 10, message: 'Max 10 characters' },
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
              maxLength: { value: 100, message: 'Max 100 characters' },
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

const DepartmentPage = () => {
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<Department | null>(null);
  const [pendingEdit, setPendingEdit] = useState<Department | null>(null);
  const [addOpen, setAddOpen] = useState(false);
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const { data: departments = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });

  const { mutate: triggerDelete, isPending: isDeletePending } = useMutation({
    mutationFn: deleteDepartment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
      setPendingDelete(null);
      setSuccessMessage('Department deleted');
      setSuccessOpen(true);
    },
    onError: () => {
      setPendingDelete(null);
      setErrorOpen(true);
    },
  });

  const onConfirmDelete = () => {
    if (pendingDelete) {
      triggerDelete(pendingDelete.code);
    }
  };

  return (
    <ProtectedPage>
      <ContentWrapper>
        <h1>Departments</h1>
        <Stack direction='row' justifyContent='flex-end' sx={{ mb: 2 }}>
          <Button variant='contained' onClick={() => setAddOpen(true)}>
            Add Department
          </Button>
        </Stack>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Code</TableCell>
                <TableCell>Name</TableCell>
                <TableCell align='right'>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {departments.map((department) => (
                <TableRow key={department.code}>
                  <TableCell>{department.code}</TableCell>
                  <TableCell>{department.name}</TableCell>
                  <TableCell align='right'>
                    <Button
                      variant='outlined'
                      size='small'
                      sx={{ mr: 1 }}
                      onClick={() => setPendingEdit(department)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='contained'
                      color='error'
                      size='small'
                      onClick={() => setPendingDelete(department)}
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
          <DialogTitle>Delete department?</DialogTitle>
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
            <DepartmentFormDialogBody
              department={pendingEdit}
              onCancel={() => {
                setPendingEdit(null);
                setAddOpen(false);
              }}
              onSuccess={(mode) => {
                setPendingEdit(null);
                setAddOpen(false);
                setSuccessMessage(mode === 'edit' ? 'Department updated' : 'Department added');
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
          message='Failed to delete department'
        />
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default DepartmentPage;
