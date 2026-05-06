import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Container,
  CssBaseline,
} from '@mui/material';

interface ContentWrapperProps {
  children: React.ReactNode;
}

const ContentWrapper: React.FC<ContentWrapperProps> = ({ children }) => {
  return (
    <Container maxWidth='xl'>
      <CssBaseline />
      <AppBar position='static'>
        <Toolbar>
          <Typography variant='h6'>University Management System</Typography>
        </Toolbar>
      </AppBar>
      {children}
    </Container>
  );
};

export default ContentWrapper;
