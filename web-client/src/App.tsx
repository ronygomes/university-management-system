import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import PageNotFound from './pages/PageNotFound';
import StudentRegistrationPage from './pages/StudentRegistrationPage';
import DepartmentPage from './pages/DepartmentPage';
import DesignationPage from './pages/DesignationPage';
import TeacherPage from './pages/TeacherPage';
import CoursePage from './pages/CoursePage';
import { AuthProvider } from './components/AuthContext';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <Router>
        <Routes>
          <Route path='/' element={<LandingPage />} />
          <Route path='/login' element={<LoginPage />} />
          <Route path='/register' element={<StudentRegistrationPage />} />
          <Route path='/admin/departments' element={<DepartmentPage />} />
          <Route path='/admin/designations' element={<DesignationPage />} />
          <Route path='/admin/teachers' element={<TeacherPage />} />
          <Route path='/admin/courses' element={<CoursePage />} />
          <Route path="*" element={<PageNotFound />} />
        </Routes>
      </Router>
    </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
