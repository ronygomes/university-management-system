import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';
import { useAuth } from '../components/AuthContext';
import AdminLandingPage from './AdminLandingPage';
import TeacherLandingPage from './TeacherLandingPage';
import StudentLandingPage from './StudentLandingPage';

const LandingPage = () => {
  const { role } = useAuth();

  let body;
  if (role === 'ADMIN') {
    body = <AdminLandingPage />;
  } else if (role === 'TEACHER') {
    body = <TeacherLandingPage />;
  } else if (role === 'STUDENT') {
    body = <StudentLandingPage />;
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
