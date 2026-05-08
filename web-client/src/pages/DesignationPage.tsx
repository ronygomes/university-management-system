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

const DESIGNATION_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/designations`;

type Designation = {
  id: number;
  title: string;
};

type DesignationFormData = {
  title: string;
};

type DesignationHalItem = {
  title: string;
  _links: { self: { href: string } };
};

function extractIdFromSelfLink(item: DesignationHalItem): number {
  const href = item._links.self.href;
  return Number(href.split('/').pop());
}

async function fetchDesignations(): Promise<Designation[]> {
  const response = await axios.get(DESIGNATION_ENDPOINT);
  const items: DesignationHalItem[] = response.data._embedded?.designations ?? [];
  return items.map((item) => ({ id: extractIdFromSelfLink(item), title: item.title }));
}

async function deleteDesignation(id: number): Promise<void> {
  await axios.delete(`${DESIGNATION_ENDPOINT}/${id}`);
}

async function updateDesignation(id: number, data: DesignationFormData): Promise<void> {
  await axios.put(`${DESIGNATION_ENDPOINT}/${id}`, data);
}

async function createDesignation(data: DesignationFormData): Promise<void> {
  await axios.post(DESIGNATION_ENDPOINT, data);
}

type ServerErrorMessage = { field: string; message: string };

interface DesignationFormDialogBodyProps {
  designation: Designation | null;
  onSuccess: (mode: 'add' | 'edit') => void;
  onCancel: () => void;
}

const DesignationFormDialogBody = ({ designation, onSuccess, onCancel }: DesignationFormDialogBodyProps) => {
  const queryClient = useQueryClient();
  const [serverErrors, setServerErrors] = useState<ServerErrorMessage[] | null>(null);
  const [fallbackError, setFallbackError] = useState<string | null>(null);
  const isEdit = designation !== null;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DesignationFormData>({
    defaultValues: { title: designation?.title ?? '' },
  });

  const { mutate, isPending } = useMutation({
    mutationFn: (data: DesignationFormData) =>
      isEdit ? updateDesignation(designation.id, data) : createDesignation(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['designations'] });
      reset({});
      onSuccess(isEdit ? 'edit' : 'add');
    },
    onError: (err: unknown) => {
      if (isAxiosError(err) && Array.isArray(err.response?.data?.errors)) {
        setServerErrors(err.response.data.errors as ServerErrorMessage[]);
      } else {
        setFallbackError(isEdit ? 'Failed to update designation' : 'Failed to add designation');
      }
    },
  });

  const onSubmit = (data: DesignationFormData) => {
    setServerErrors(null);
    setFallbackError(null);
    mutate(data);
  };

  return (
    <>
      <DialogTitle>{isEdit ? 'Edit designation' : 'Add designation'}</DialogTitle>
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

const DesignationPage = () => {
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<Designation | null>(null);
  const [pendingEdit, setPendingEdit] = useState<Designation | null>(null);
  const [addOpen, setAddOpen] = useState(false);
  const [successOpen, setSuccessOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const { data: designations = [] } = useQuery({
    queryKey: ['designations'],
    queryFn: fetchDesignations,
  });

  const { mutate: triggerDelete, isPending: isDeletePending } = useMutation({
    mutationFn: deleteDesignation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['designations'] });
      setPendingDelete(null);
      setSuccessMessage('Designation deleted');
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
        <h1>Designations</h1>
        <Stack direction='row' justifyContent='flex-end' sx={{ mb: 2 }}>
          <Button variant='contained' onClick={() => setAddOpen(true)}>
            Add Designation
          </Button>
        </Stack>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Title</TableCell>
                <TableCell align='right'>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {designations.map((designation) => (
                <TableRow key={designation.id}>
                  <TableCell>{designation.title}</TableCell>
                  <TableCell align='right'>
                    <Button
                      variant='outlined'
                      size='small'
                      sx={{ mr: 1 }}
                      onClick={() => setPendingEdit(designation)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='contained'
                      color='error'
                      size='small'
                      onClick={() => setPendingDelete(designation)}
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
          <DialogTitle>Delete designation?</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Are you sure you want to delete &quot;{pendingDelete?.title}&quot;? This cannot be undone.
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
            <DesignationFormDialogBody
              designation={pendingEdit}
              onCancel={() => {
                setPendingEdit(null);
                setAddOpen(false);
              }}
              onSuccess={(mode) => {
                setPendingEdit(null);
                setAddOpen(false);
                setSuccessMessage(mode === 'edit' ? 'Designation updated' : 'Designation added');
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
          message='Failed to delete designation'
        />
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default DesignationPage;
