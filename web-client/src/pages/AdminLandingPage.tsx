import { Box, Button, Card, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

const AdminLandingPage = () => {
  return (
    <>
      <h1>Welcome Admin</h1>
      <Stack spacing={2}>
        <Card sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography>Department</Typography>
            <Button component={RouterLink} to='/admin/departments' variant='outlined'>
              View
            </Button>
          </Box>
        </Card>
        <Card sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography>Designation</Typography>
            <Button component={RouterLink} to='/admin/designations' variant='outlined'>
              View
            </Button>
          </Box>
        </Card>
        <Card sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography>Teacher</Typography>
            <Button component={RouterLink} to='/admin/teachers' variant='outlined'>
              View
            </Button>
          </Box>
        </Card>
        <Card sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography>Course</Typography>
            <Button component={RouterLink} to='/admin/courses' variant='outlined'>
              View
            </Button>
          </Box>
        </Card>
        <Card sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography>Schedule</Typography>
            <Button component={RouterLink} to='/admin/schedules' variant='outlined'>
              View
            </Button>
          </Box>
        </Card>
      </Stack>
    </>
  );
};

export default AdminLandingPage;
