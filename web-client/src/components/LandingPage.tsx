import { useEffect, useState } from 'react';
import axios from 'axios';

const DEPARTMENT_ENDPOINT = `${import.meta.env.VITE_API_SERVER_URL}/v1/departments`;

type Department = {
  code: string,
  name: string,
}

const LandingPage = () => {
  const [departments, setDepartments] = useState<Department[]>([]);

  useEffect(() => {
    // TODO: Move it to AuthProvider and keep in memory
    const accessToken = sessionStorage.getItem('jwtToken');

    axios.get(DEPARTMENT_ENDPOINT, {
            headers: { 'Authorization': 'Bearer ' + accessToken }
        }).then(response => {
          setDepartments(response.data._embedded.departments);
        });
  }, []);

  return (
    <>
      <h1>Welcome Authenticated User</h1>
      <ul>
        {departments.map((department) => (
            <li key={department.code}>{department.name}</li>
          ))}
      </ul>
    </>
  );
};

export default LandingPage;
