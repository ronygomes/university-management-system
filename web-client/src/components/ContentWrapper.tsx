import React from 'react';
import {
  AppBar,
  Box,
  Toolbar,
  Typography,
  Container,
  CssBaseline,
  IconButton,
} from '@mui/material';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import { useNavigate } from 'react-router-dom';
import { useAuth, type Role } from './AuthContext';

interface ContentWrapperProps {
  children: React.ReactNode;
}

const ROLE_LABEL: Record<Role, string> = {
  ADMIN: 'Admin',
  TEACHER: 'Teacher',
  STUDENT: 'Student',
};

const ContentWrapper: React.FC<ContentWrapperProps> = ({ children }) => {
  const { username, role, logoutHandler } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutHandler();
    navigate('/login');
  };

  const userLabel = role ? `${username} (${ROLE_LABEL[role]})` : username;

  return (
    <Container maxWidth='xl'>
      <CssBaseline />
      <AppBar position='static'>
        <Toolbar>
          <Typography variant='h6'>University Management System</Typography>
          <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
            {userLabel && <Typography variant='body1'>{userLabel}</Typography>}
            <IconButton color='inherit' aria-label='Logout' onClick={handleLogout}>
              <PowerSettingsNewIcon />
            </IconButton>
          </Box>
        </Toolbar>
      </AppBar>
      {children}
    </Container>
  );
};

export default ContentWrapper;
