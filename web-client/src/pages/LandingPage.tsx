import axios from 'axios';
import { useQuery } from '@tanstack/react-query';
import ContentWrapper from '../components/ContentWrapper';
import ProtectedPage from '../components/ProtectedPage';

const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;

type Department = {
  code: string;
  name: string;
};

async function fetchDepartments(): Promise<Department[]> {
  const response = await axios.get(DEPARTMENT_ENDPOINT);
  return response.data._embedded.departments;
}

const LandingPage = () => {
  const { data: departments = [] } = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartments,
  });

  return (
    <ProtectedPage>
      <ContentWrapper>
        <h1>Welcome Authenticated User</h1>
        <ul>
          {departments.map((department) => (
            <li key={department.code}>{department.name}</li>
          ))}
        </ul>
      </ContentWrapper>
    </ProtectedPage>
  );
};

export default LandingPage;
