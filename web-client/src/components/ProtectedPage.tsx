import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

interface ProtectedPageProps {
    children: React.ReactNode;
}

const ProtectedPage : React.FC<ProtectedPageProps> = ({ children }) => {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default ProtectedPage;
