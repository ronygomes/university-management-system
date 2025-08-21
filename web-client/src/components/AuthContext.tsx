import  { useState, useContext, createContext, type ReactNode } from 'react';
import axios, { type AxiosResponse } from 'axios';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

type User = {
  email: string;
  password: string;
}

interface AuthContextType {
    isAuthenticated: boolean;
    token: AccessToken | null;
    loginHandler: (user: User) => Promise<boolean>;
    logoutHandler: () => void;
}

type AccessToken = {
    access_token: string;
    expires_in: number;
    // not-before-policy
    refresh_expires_in: number;
    refresh_token: string;
    scope: string;
    token_type: string;
}

const TOKEN_ENDPOINT = `${import.meta.env.VITE_AUTH_SERVER_URL}/realms/ums/protocol/openid-connect/token`;

async function fetchAccessToken(user: User): Promise<AccessToken> {
    try {
        const response: AxiosResponse<AccessToken> = await axios.post<AccessToken>(TOKEN_ENDPOINT, {
            'grant_type': 'password',
            'username': user.email,
            'password': user.password,
            'client_id': 'ums-client-webapp',
            'redirect_uri': 'http://localhost:3000/'
        }, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });

        return response.data;
    } catch (error) {
        throw error;
    }
}

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider = ( { children } : AuthProviderProps ) => {
    const [isAuthenticated, setAuthenticated] = useState(false);
    const [token, setToken] = useState<AccessToken | null>(null);

    const loginHandler = async (user: User) : Promise<boolean> => {
        try {
            const data = await fetchAccessToken(user);
            setToken(data);
            setAuthenticated(true);
            return true;
        } catch (error) {
            setAuthenticated(false);
            return false;
        }
    };

    const logoutHandler = () => {
        setAuthenticated(false);
    };

    return (
        <AuthContext.Provider value={{isAuthenticated, token, loginHandler, logoutHandler}}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
};
