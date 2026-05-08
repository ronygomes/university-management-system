import {
  Avatar,
  Box,
  Button,
  Checkbox,
  Container,
  FormControlLabel,
  Paper,
  Snackbar,
  TextField,
  Typography,
  Grid,
  Link,
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { useState } from 'react';
import { useAuth } from '../components/AuthContext';
import { Navigate } from 'react-router-dom';

type User = {
  username: string;
  password: string;
};

const LoginPage = () => {
  const [formUser, setFormUser] = useState<User>({
    username: '',
    password: '',
  });
  const [remember, setRemember] = useState(true);

  const [open, setOpen] = useState(false);
  const { isAuthenticated, loginHandler } = useAuth();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormUser({ ...formUser, [event.target.name]: event.target.value });
  };

  const handleSubmit = async (event: React.SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();
    const isLoginSuccess = await loginHandler(formUser, remember);
    setOpen(!isLoginSuccess);
  };

  if (isAuthenticated) {
    return <Navigate to='/' replace />;
  }

  return (
    <Container maxWidth='xs'>
      <Paper elevation={10} sx={{ marginTop: 8, padding: 2 }}>
        <Avatar
          sx={{
            mx: 'auto',
            bgcolor: 'secondary.main',
            mb: 1,
          }}
        >
          <LockOutlinedIcon />
        </Avatar>
        <Typography component='h1' variant='h5' sx={{ textAlign: 'center' }}>
          Sign In
        </Typography>

        <Box component='form' onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField
            placeholder='Username'
            fullWidth
            required
            autoFocus
            name='username'
            sx={{ mb: 2 }}
            onChange={handleChange}
          />

          <TextField
            placeholder='Password'
            fullWidth
            required
            name='password'
            type='password'
            sx={{ mb: 2 }}
            onChange={handleChange}
          />

          <FormControlLabel
            control={
              <Checkbox
                color='primary'
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
            }
            label='Remember me'
          />

          <Button type='submit' variant='contained' fullWidth sx={{ mt: 1 }}>
            Sign In
          </Button>
        </Box>

        <Grid container justifyContent='space-between' sx={{ mt: 1 }}>
          <Grid>
            <Link href='/forgot' underline='always' variant='body2'>
              {'Forgot Password?'}
            </Link>
          </Grid>

          <Grid>
            <Link href='/register' underline='always' variant='body2'>
              {'Sign Up'}
            </Link>
          </Grid>
        </Grid>
      </Paper>
      <Snackbar
        open={open}
        autoHideDuration={3000}
        onClose={() => setOpen(false)}
        message='Invalid username or password'
      />
    </Container>
  );
};

export default LoginPage;
