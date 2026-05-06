import { useEffect, useState } from 'react';
import axios from 'axios';
import ContentWrapper from '../components/ContentWrapper';
import { useAuth } from '../components/AuthContext';
import ProtectedPage from '../components/ProtectedPage';

const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;

type Department = {
  code: string;
  name: string;
};

const LandingPage = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const { token } = useAuth();

  useEffect(() => {
    if (token != null) {
      axios
        .get(DEPARTMENT_ENDPOINT, {
          headers: { Authorization: 'Bearer ' + token.access_token },
        })
        .then((response) => {
          setDepartments(response.data._embedded.departments);
        });
    }
  }, [token]);

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
