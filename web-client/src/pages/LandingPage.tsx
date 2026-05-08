import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';
import { useAuth } from '../components/AuthContext';
import AdminLandingPage from './AdminLandingPage';

const ROLE_LABEL = {
  TEACHER: 'Teacher',
  STUDENT: 'Student',
} as const;

const LandingPage = () => {
  const { role } = useAuth();

  return (
    <ProtectedPage>
      <ContentWrapper>
        {role === 'ADMIN' ? (
          <AdminLandingPage />
        ) : (
          <h1>Welcome {role && role !== 'ADMIN' ? ROLE_LABEL[role] : ''}</h1>
        )}
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default LandingPage;
