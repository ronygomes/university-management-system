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
import axios, { type AxiosResponse } from 'axios';
import LandingPage from './LandingPage';

type User = {
  email: string;
  password: string;
}

type AccessToken = {
    access_token: string;
    expires_in: number;
    // not-before-policy
    refresh_expires_in: number;
    refresh_token: string;
    scope: string;
    token_type: string;
}

const TOKEN_ENDPOINT = `${import.meta.env.VITE_AUTH_SERVER_URL}/realms/ums/protocol/openid-connect/token`;

async function fetchAccessToken(user: User): Promise<AccessToken> {
    try {
        const response: AxiosResponse<AccessToken> = await axios.post<AccessToken>(TOKEN_ENDPOINT, {
            'grant_type': 'password', 
            'username': user.email,  
            'password': user.password, 
            'client_id': 'ums-client-webapp', 
            'redirect_uri': 'http://localhost:3000/'
        }, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });

        console.log(response.data);
        return response.data;
    } catch (error) {
        throw error;
    }
}

const LoginPage = () => {

    const [user, setUser] = useState<User>({
        email: '',
        password: ''
    });

    const [isAuthenticated, setAuth] = useState(false);
    const [open, setOpen] = useState(false);


    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setUser({...user, [event.target.name] : event.target.value});
    }

    const handleSubmit = async (event: React.SyntheticEvent<HTMLFormElement>) => {
        event.preventDefault();

        try {
            const data = await fetchAccessToken(user);
            sessionStorage.setItem('jwtToken', data.access_token);
            setAuth(true);
            
        } catch (error) {
            setOpen(true);
        }
    }


    if (isAuthenticated) {
        return <LandingPage />;
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

            <Box component='form'
                onSubmit={handleSubmit}
                noValidate
                sx={{ mt: 1 }}
            >
                <TextField
                    placeholder='Email'
                    fullWidth required autoFocus
                    name='email'
                    sx={{ mb: 2 }}
                    onChange={handleChange}
                />

                <TextField
                    placeholder='Password'
                    fullWidth required
                    name='password'
                    type='password'
                    sx={{ mb: 2 }}
                    onChange={handleChange}
                />

                <FormControlLabel control={<Checkbox value='remember' color='primary'/>}
                    label='Remember me'
                />

                <Button type='submit' variant='contained' fullWidth sx={{mt: 1}}>
                    Sign In
                </Button>
            </Box>

            <Grid container justifyContent='space-between' sx={{mt: 1}}>
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
                message="Invalid email or password"
            />
        </Container>
    );
};

export default LoginPage;
