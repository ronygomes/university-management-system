import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';
import { useAuth } from '../components/AuthContext';
import AdminLandingPage from './AdminLandingPage';
import TeacherLandingPage from './TeacherLandingPage';

const LandingPage = () => {
  const { role } = useAuth();

  let body;
  if (role === 'ADMIN') {
    body = <AdminLandingPage />;
  } else if (role === 'TEACHER') {
    body = <TeacherLandingPage />;
  } else if (role === 'STUDENT') {
    body = <h1>Welcome Student</h1>;
  } else {
    body = <h1>Welcome</h1>;
  }

  return (
    <ProtectedPage>
      <ContentWrapper>{body}</ContentWrapper>
    </ProtectedPage>
  );
};

export default LandingPage;
