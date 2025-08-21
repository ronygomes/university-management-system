import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  AppBar,
  Toolbar,
  Typography,
  Container,
  CssBaseline,
}  from '@mui/material';

interface ContentWrapperProps {
    children: React.ReactNode;
}

const queryClient = new QueryClient();

const ContentWrapper: React.FC<ContentWrapperProps> = ({ children }) => {
  return (
      <Container maxWidth="xl">
      <CssBaseline />
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">
            University Management System
          </Typography>
        </Toolbar>
      </AppBar>
      <QueryClientProvider client={queryClient}>
        { children }
      </QueryClientProvider>
    </Container>
  );
};

export default ContentWrapper;
