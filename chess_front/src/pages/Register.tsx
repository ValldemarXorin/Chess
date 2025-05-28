import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Crown, Mail, Lock, User, Eye, EyeOff, CheckCircle, X } from 'lucide-react';

const Register: React.FC = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [passwordValidation, setPasswordValidation] = useState({
        length: false,
        uppercase: false,
        lowercase: false,
        number: false,
        special: false
    });

    const { register, isAuthenticated, error, clearError } = useAuth();
    const navigate = useNavigate();

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            navigate('/');
        }
    }, [isAuthenticated, navigate]);

    useEffect(() => {
        clearError();
    }, [location.pathname]); // Отдельный эффект только для сброса

    // Clear errors when component mounts or form changes
    useEffect(() => {
        clearError();
    }, [formData, clearError]);

    // Validate password in real-time
    useEffect(() => {
        const { password } = formData;
        setPasswordValidation({
            length: password.length >= 8,
            uppercase: /[A-Z]/.test(password),
            lowercase: /[a-z]/.test(password),
            number: /\d/.test(password),
            special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
        });
    }, [formData.password]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const isFormValid = () => {
        const { name, email, password, confirmPassword } = formData;
        const isPasswordValid = Object.values(passwordValidation).every(Boolean);
        const passwordsMatch = password === confirmPassword;
        const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

        return name.trim() && emailValid && isPasswordValid && passwordsMatch;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!isFormValid()) {
            return;
        }

        setIsLoading(true);

        try {
            await register(formData.name, formData.email, formData.password);
            navigate('/');
        } catch (error) {
            console.error('Registration failed:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const ValidationItem: React.FC<{ isValid: boolean; text: string }> = ({ isValid, text }) => (
        <div className={`flex items-center space-x-2 text-sm ${isValid ? 'text-green-400' : 'text-gray-400'}`}>
            {isValid ? (
                <CheckCircle className="w-4 h-4" />
            ) : (
                <X className="w-4 h-4" />
            )}
            <span>{text}</span>
        </div>
    );

    return (
        <div className="min-h-screen flex items-center justify-center bg-chess-dark px-4 py-8">
            <div className="max-w-md w-full space-y-8">
                {/* Header */}
                <div className="text-center">
                    <div className="flex items-center justify-center mb-6">
                        <div className="w-16 h-16 bg-gradient-gold rounded-full flex items-center justify-center">
                            <Crown className="w-10 h-10 text-black" />
                        </div>
                    </div>
                    <h2 className="text-4xl font-bold text-white mb-2">Join Chess Arena</h2>
                    <p className="text-gray-400">
                        Create your account and start your journey to chess mastery
                    </p>
                </div>

                {/* Registration Form */}
                <div className="glass-effect rounded-lg p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Error Display */}
                        {error && (
                            <div className="bg-red-900 bg-opacity-50 border border-red-500 text-red-200 px-4 py-3 rounded-lg text-sm">
                                {error}
                            </div>
                        )}

                        {/* Name Field */}
                        <div>
                            <label htmlFor="name" className="block text-sm font-medium text-gray-300 mb-2">
                                Full Name
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <User className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    id="name"
                                    name="name"
                                    type="text"
                                    autoComplete="name"
                                    required
                                    value={formData.name}
                                    onChange={handleInputChange}
                                    className="block w-full pl-10 pr-3 py-3 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200"
                                    placeholder="Enter your full name"
                                />
                            </div>
                        </div>

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
                                    name="email"
                                    type="email"
                                    autoComplete="email"
                                    required
                                    value={formData.email}
                                    onChange={handleInputChange}
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
                                    name="password"
                                    type={showPassword ? 'text' : 'password'}
                                    autoComplete="new-password"
                                    required
                                    value={formData.password}
                                    onChange={handleInputChange}
                                    className="block w-full pl-10 pr-12 py-3 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200"
                                    placeholder="Create a password"
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

                            {/* Password Requirements */}
                            {formData.password && (
                                <div className="mt-3 p-3 bg-chess-darker rounded-lg space-y-1">
                                    <p className="text-xs text-gray-400 mb-2">Password must contain:</p>
                                    <ValidationItem isValid={passwordValidation.length} text="At least 8 characters" />
                                    <ValidationItem isValid={passwordValidation.uppercase} text="One uppercase letter" />
                                    <ValidationItem isValid={passwordValidation.lowercase} text="One lowercase letter" />
                                    <ValidationItem isValid={passwordValidation.number} text="One number" />
                                    <ValidationItem isValid={passwordValidation.special} text="One special character" />
                                </div>
                            )}
                        </div>

                        {/* Confirm Password Field */}
                        <div>
                            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-300 mb-2">
                                Confirm Password
                            </label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Lock className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    autoComplete="new-password"
                                    required
                                    value={formData.confirmPassword}
                                    onChange={handleInputChange}
                                    className="block w-full pl-10 pr-12 py-3 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200"
                                    placeholder="Confirm your password"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-300"
                                >
                                    {showConfirmPassword ? (
                                        <EyeOff className="h-5 w-5" />
                                    ) : (
                                        <Eye className="h-5 w-5" />
                                    )}
                                </button>
                            </div>

                            {/* Password Match Indicator */}
                            {formData.confirmPassword && (
                                <div className="mt-2">
                                    <ValidationItem
                                        isValid={formData.password === formData.confirmPassword}
                                        text="Passwords match"
                                    />
                                </div>
                            )}
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={isLoading || !isFormValid()}
                            className="w-full btn-chess py-3 rounded-lg font-semibold text-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:transform-none"
                        >
                            {isLoading ? (
                                <div className="flex items-center justify-center">
                                    <div className="spinner mr-2"></div>
                                    Creating Account...
                                </div>
                            ) : (
                                'Create Account'
                            )}
                        </button>
                    </form>

                    {/* Links */}
                    <div className="mt-6 text-center space-y-2">
                        <p className="text-gray-400 text-sm">
                            Already have an account?{' '}
                            <Link
                                to="/login"
                                className="text-chess-gold hover:text-chess-gold-light transition-colors duration-200 font-medium"
                            >
                                Sign in here
                            </Link>
                        </p>
                    </div>
                </div>

                {/* Features */}
                <div className="text-center">
                    <p className="text-gray-500 text-sm mb-4">What you'll get with Chess Arena</p>
                    <div className="grid grid-cols-2 gap-4 text-xs text-gray-400">
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">✓ Free Play</div>
                            <div>Unlimited games</div>
                        </div>
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">✓ Friends</div>
                            <div>Connect & play</div>
                        </div>
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">✓ Rankings</div>
                            <div>Track progress</div>
                        </div>
                        <div className="text-center">
                            <div className="text-chess-gold font-semibold">✓ History</div>
                            <div>Game analysis</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Register;