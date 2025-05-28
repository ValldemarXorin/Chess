import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Index = () => {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-chess-dark">
                <div className="text-center">
                    <div className="spinner mx-auto mb-4"></div>
                    <p className="text-chess-gold">Loading Chess Arena...</p>
                </div>
            </div>
        );
    }

    // Redirect based on authentication status
    return <Navigate to={isAuthenticated ? "/" : "/login"} replace />;
};

export default Index;