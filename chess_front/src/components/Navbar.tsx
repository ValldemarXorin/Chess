import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { Crown, LogOut, User, Users, Search, History } from 'lucide-react';

const Navbar: React.FC = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const navItems = [
        { path: '/', label: 'Dashboard', icon: Crown },
        { path: '/friends', label: 'Friends', icon: Users },
        { path: '/search', label: 'Search', icon: Search },
        { path: '/history', label: 'History', icon: History },
        { path: '/profile', label: 'Profile', icon: User },
    ];

    const isActive = (path: string) => {
        return location.pathname === path;
    };

    return (
        <nav className="bg-chess-darker border-b border-chess-gold border-opacity-20 sticky top-0 z-40">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Logo */}
                    <div className="flex items-center">
                        <div className="flex-shrink-0">
                            <h1 className="text-2xl font-bold text-chess-gold flex items-center gap-2">
                                <Crown className="w-8 h-8" />
                                Chess Arena
                            </h1>
                        </div>
                    </div>

                    {/* Navigation Links */}
                    <div className="hidden md:block">
                        <div className="ml-10 flex items-baseline space-x-4">
                            {navItems.map((item) => {
                                const Icon = item.icon;
                                return (
                                    <button
                                        key={item.path}
                                        onClick={() => navigate(item.path)}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                                            isActive(item.path)
                                                ? 'bg-chess-gold text-black'
                                                : 'text-gray-300 hover:text-chess-gold hover:bg-chess-dark'
                                        }`}
                                    >
                                        <Icon className="w-4 h-4" />
                                        {item.label}
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* User Menu */}
                    <div className="flex items-center space-x-4">
                        {user && (
                            <div className="flex items-center space-x-3">
                                <div className="text-right">
                                    <p className="text-sm font-medium text-white">{user.name}</p>
                                    <p className="text-xs text-gray-400">{user.email}</p>
                                </div>
                                <div className="status-indicator online">
                                    <div className="w-10 h-10 bg-chess-gold rounded-full flex items-center justify-center">
                                        <User className="w-5 h-5 text-black" />
                                    </div>
                                </div>
                            </div>
                        )}

                        <button
                            onClick={handleLogout}
                            className="text-gray-300 hover:text-red-400 transition-colors duration-200 p-2"
                            title="Logout"
                        >
                            <LogOut className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Mobile menu button */}
                    <div className="md:hidden">
                        <button
                            onClick={() => {/* TODO: Implement mobile menu */}}
                            className="text-gray-300 hover:text-chess-gold p-2"
                        >
                            <svg className="w-6 h-6" fill="none" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" viewBox="0 0 24 24" stroke="currentColor">
                                <path d="M4 6h16M4 12h16M4 18h16"></path>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile Navigation */}
            <div className="md:hidden border-t border-chess-gold border-opacity-20">
                <div className="px-2 pt-2 pb-3 space-y-1">
                    {navItems.map((item) => {
                        const Icon = item.icon;
                        return (
                            <button
                                key={item.path}
                                onClick={() => navigate(item.path)}
                                className={`w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                                    isActive(item.path)
                                        ? 'bg-chess-gold text-black'
                                        : 'text-gray-300 hover:text-chess-gold hover:bg-chess-dark'
                                }`}
                            >
                                <Icon className="w-4 h-4" />
                                {item.label}
                            </button>
                        );
                    })}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;