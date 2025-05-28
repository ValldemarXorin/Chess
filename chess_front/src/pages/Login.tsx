import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Crown, Mail, Lock, Eye, EyeOff } from 'lucide-react';

const Login: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const { login, isAuthenticated, error, clearError } = useAuth();
    const navigate = useNavigate();

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            navigate('/');
        }
    }, [isAuthenticated, navigate]);

    useEffect(() => {
        setEmail('');
        setPassword('');
        clearError();
    }, [location.pathname]); // Отдельный эффект только для сброса

    // Clear errors when component mounts or form changes
    useEffect(() => {
        clearError();
    }, [email, password, clearError]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!email || !password) {
            return;
        }

        setIsLoading(true);

        try {
            await login(email, password);
            navigate('/');
        } catch (error) {
            console.error('Login failed:', error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-chess-dark px-4">
            <div className="max-w-md w-full space-y-8">
                {/* Header */}
                <div className="text-center">
                    <div className="flex items-center justify-center mb-6">
                        <div className="w-16 h-16 bg-gradient-gold rounded-full flex items-center justify-center">
                            <Crown className="w-10 h-10 text-black" />
                        </div>
                    </div>
                    <h2 className="text-4xl font-bold text-white mb-2">Welcome Back</h2>
                    <p className="text-gray-400">
                        Sign in to your Chess Arena account and continue your journey to chess mastery
                    </p>
                </div>

                {/* Login Form */}
                <div className="glass-effect rounded-lg p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Error Display */}
                        {error && (
                            <div className="bg-red-900 bg-opacity-50 border border-red-500 text-red-200 px-4 py-3 rounded-lg text-sm">
                                {error}
                            </div>
                        )}

                        {/* Email Field */}
                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-300 mb-2">
                                Email Address
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Mail className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    id="email"
                                    type="email"
                                    autoComplete="email"
                                    required
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="block w-full pl-10 pr-3 py-3 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200"
                                    placeholder="Enter your email"
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div>
                            <label htmlFor="password" className="block text-sm font-medium text-gray-300 mb-2">
                                Password
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Lock className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    autoComplete="current-password"
                                    required
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="block w-full pl-10 pr-12 py-3 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200"
                                    placeholder="Enter your password"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-300"
                                >
                                    {showPassword ? (
                                        <EyeOff className="h-5 w-5" />
                                    ) : (
                                        <Eye className="h-5 w-5" />
                                    )}
                                </button>
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={isLoading || !email || !password}
                            className="w-full btn-chess py-3 rounded-lg font-semibold text-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:transform-none"
                        >
                            {isLoading ? (
                                <div className="flex items-center justify-center">
                                    <div className="spinner mr-2"></div>
                                    Signing In...
                                </div>
                            ) : (
                                'Sign In'
                            )}
                        </button>
                    </form>

                    {/* Links */}
                    <div className="mt-6 text-center space-y-2">
                        <p className="text-gray-400 text-sm">
                            Don't have an account?{' '}
                            <Link
                                to="/register"
                                className="text-chess-gold hover:text-chess-gold-light transition-colors duration-200 font-medium"
                            >
                                Sign up here
                            </Link>
                        </p>
                    </div>
                </div>

                {/* Features */}
                <div className="text-center">
                    <p className="text-gray-500 text-sm mb-4">Join thousands of chess players worldwide</p>
                    <div className="flex justify-center space-x-8 text-xs text-gray-400">
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">10K+</div>
                            <div>Active Players</div>
                        </div>
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">50K+</div>
                            <div>Games Daily</div>
                        </div>
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">24/7</div>
                            <div>Matchmaking</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;