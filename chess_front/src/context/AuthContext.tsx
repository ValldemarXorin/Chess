import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { PlayerResponse, AuthState } from '../types';
import { authAPI } from '../services/api';
import webSocketService from '../services/websocket';

// Auth actions
type AuthAction =
    | { type: 'LOGIN_START' }
    | { type: 'LOGIN_SUCCESS'; payload: PlayerResponse }
    | { type: 'LOGIN_FAILURE'; payload: string }
    | { type: 'LOGOUT' }
    | { type: 'REGISTER_START' }
    | { type: 'REGISTER_SUCCESS'; payload: PlayerResponse }
    | { type: 'REGISTER_FAILURE'; payload: string }
    | { type: 'CLEAR_ERROR' }
    | { type: 'UPDATE_USER'; payload: PlayerResponse }; // Новое действие

// Initial state
const initialState: AuthState = {
    user: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
};

// Auth reducer
const authReducer = (state: AuthState, action: AuthAction): AuthState => {
    switch (action.type) {
        case 'LOGIN_START':
        case 'REGISTER_START':
            return {
                ...state,
                isLoading: true,
                error: null,
            };
        case 'LOGIN_SUCCESS':
        case 'REGISTER_SUCCESS':
            return {
                ...state,
                user: action.payload,
                isAuthenticated: true,
                isLoading: false,
                error: null,
            };
        case 'LOGIN_FAILURE':
        case 'REGISTER_FAILURE':
            return {
                ...state,
                user: null,
                isAuthenticated: false,
                isLoading: false,
                error: action.payload,
            };
        case 'LOGOUT':
            return {
                ...state,
                user: null,
                isAuthenticated: false,
                isLoading: false,
                error: null,
            };
        case 'CLEAR_ERROR':
            return {
                ...state,
                error: null,
            };
        case 'UPDATE_USER': // Обработка нового действия
            return {
                ...state,
                user: action.payload,
                isAuthenticated: true,
                isLoading: false,
                error: null,
            };
        default:
            return state;
    }
};

// Auth context type
interface AuthContextType extends AuthState {
    login: (email: string, password: string) => Promise<void>;
    register: (name: string, email: string, password: string) => Promise<void>;
    logout: () => void;
    clearError: () => void;
    updateUser: (user: PlayerResponse) => void; // Новая функция
}

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth provider component
interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [state, dispatch] = useReducer(authReducer, initialState);

    // Initialize auth state from localStorage
    useEffect(() => {
        const savedUser = localStorage.getItem('currentUser');
        const token = localStorage.getItem('authToken');

        if (savedUser && token) {
            try {
                const user: PlayerResponse = JSON.parse(savedUser);
                dispatch({ type: 'LOGIN_SUCCESS', payload: user });

                // Connect to WebSocket if user is authenticated
                webSocketService.connect(user.id).catch((error) => {
                    console.error('Failed to connect to WebSocket:', error);
                });
            } catch (error) {
                console.error('Invalid saved user data:', error);
                localStorage.removeItem('currentUser');
                localStorage.removeItem('authToken');
            }
        }
    }, []);

    // Login function
    const login = async (email: string, password: string): Promise<void> => {
        dispatch({ type: 'LOGIN_START' });

        try {
            const user = await authAPI.login({ email, password });

            // Save user and token to localStorage
            localStorage.setItem('currentUser', JSON.stringify(user));
            localStorage.setItem('authToken', 'jwt-token-placeholder'); // Backend should return actual token

            dispatch({ type: 'LOGIN_SUCCESS', payload: user });

            // Connect to WebSocket after successful login
            try {
                await webSocketService.connect(user.id);
            } catch (wsError) {
                console.error('WebSocket connection failed after login:', wsError);
                // Don't fail login due to WebSocket issues
            }
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || 'Login failed. Please check your credentials.';
            dispatch({ type: 'LOGIN_FAILURE', payload: errorMessage });
            throw error;
        }
    };

    // Register function
    const register = async (name: string, email: string, password: string): Promise<void> => {
        dispatch({ type: 'REGISTER_START' });

        try {
            const user = await authAPI.register({ name, email, password });

            // Save user and token to localStorage
            localStorage.setItem('currentUser', JSON.stringify(user));
            localStorage.setItem('authToken', 'jwt-token-placeholder'); // Backend should return actual token

            dispatch({ type: 'REGISTER_SUCCESS', payload: user });

            // Connect to WebSocket after successful registration
            try {
                await webSocketService.connect(user.id);
            } catch (wsError) {
                console.error('WebSocket connection failed after registration:', wsError);
                // Don't fail registration due to WebSocket issues
            }
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || 'Registration failed. Please try again.';
            dispatch({ type: 'REGISTER_FAILURE', payload: errorMessage });
            throw error;
        }
    };

    // Logout function
    const logout = (): void => {
        authAPI.logout();
        webSocketService.disconnect();
        dispatch({ type: 'LOGOUT' });
    };

    // Clear error function
    const clearError = (): void => {
        dispatch({ type: 'CLEAR_ERROR' });
    };

    // Update user function
    const updateUser = (user: PlayerResponse): void => {
        localStorage.setItem('currentUser', JSON.stringify(user)); // Синхронизируем localStorage
        dispatch({ type: 'UPDATE_USER', payload: user });
    };

    const value: AuthContextType = {
        ...state,
        login,
        register,
        logout,
        clearError,
        updateUser, // Добавляем функцию
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export default AuthContext;