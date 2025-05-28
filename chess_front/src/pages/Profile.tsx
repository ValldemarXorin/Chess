import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import {GameInfoResponse, PlayerResponse} from '../types';
import { playersAPI } from '../services/api';
import { User, Mail, Trophy, Target, Calendar, TrendingUp, Edit, Save, X } from 'lucide-react';
import { useToast } from '../hooks/use-toast';
import {useNavigate} from "react-router-dom";

const Profile: React.FC = () => {
    const navigate = useNavigate()
    const { user, updateUser } = useAuth(); // Добавляем updateUser
    const { toast } = useToast();

    const [games, setGames] = useState<GameInfoResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [editForm, setEditForm] = useState({
        name: user?.name || '',
        email: user?.email || ''
    });

    useEffect(() => {
        if (user) {
            loadProfileData();
            setEditForm({
                name: user.name,
                email: user.email
            });
        }
    }, [user]);

    const loadProfileData = async () => {
        if (!user) return;

        setIsLoading(true);
        try {
            const gamesData = await playersAPI.getPlayerGames(user.id);
            setGames(gamesData);
        } catch (error) {
            console.error('Failed to load profile data:', error);
            toast({
                title: "Error",
                description: "Failed to load profile data. Please try again.",
                variant: "destructive"
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleSaveProfile = async () => {
        if (!user) return;

        try {
            const updatedPlayer: PlayerResponse = await playersAPI.updatePlayer(user.id, editForm);
            setIsEditing(false);
            // Обновляем user в контексте
            updateUser(updatedPlayer);
            toast({
                title: "Profile Updated",
                description: "Your profile has been updated successfully!",
            });
        } catch (error) {
            console.error('Failed to update profile:', error);
            toast({
                title: "Error",
                description: "Failed to update profile. Please try again.",
                variant: "destructive"
            });
        }
    };

    const handleCancelEdit = () => {
        setIsEditing(false);
        setEditForm({
            name: user?.name || '',
            email: user?.email || ''
        });
    };

    // Calculate statistics
    const totalGames = games.length;
    const wins = games.filter(game => {
        // Simplified win calculation - in real app this would be more complex
        return game.status === 'White win' && game.whitePlayer.id === user?.id
            || game.status === 'Black win' && game.blackPlayer.id === user?.id;
    }).length;
    const draws = games.filter(game => {
        // Simplified win calculation - in real app this would be more complex
        return game.status === 'Draw'
    }).length;
    const losses = games.length - wins - draws;
    const winRate = totalGames > 0 ? Math.round((wins / totalGames) * 100) : 0;

    // Mock rating - in real app this would come from backend
    const currentRating = 1022;

    const memberSince = new Date('2025-05-15'); // Mock date
    const formatMemberSince = memberSince.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long'
    });

    if (isLoading) {
        return (
            <div className="min-h-screen bg-chess-dark">
                <Navbar />
                <div className="flex items-center justify-center h-96">
                    <div className="text-center">
                        <div className="spinner mx-auto mb-4"></div>
                        <p className="text-chess-gold">Loading profile...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-chess-dark">
            <Navbar />

            <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-4xl font-bold text-white mb-2 flex items-center gap-3">
                        <User className="w-10 h-10 text-chess-gold" />
                        My Profile
                    </h1>
                    <p className="text-gray-400 text-lg">
                        Manage your account and view your chess statistics
                    </p>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Profile Information */}
                    <div className="lg:col-span-1">
                        <div className="glass-effect rounded-lg p-6 mb-6">
                            <div className="text-center mb-6">
                                <div className="w-24 h-24 bg-chess-gold rounded-full flex items-center justify-center mx-auto mb-4">
                                    <User className="w-12 h-12 text-black" />
                                </div>

                                {!isEditing ? (
                                    <div>
                                        <h2 className="text-2xl font-bold text-white mb-1">{user?.name}</h2>
                                        <p className="text-gray-400 mb-4">{user?.email}</p>
                                        <button
                                            onClick={() => setIsEditing(true)}
                                            className="px-4 py-2 bg-chess-dark hover:bg-chess-gold hover:text-black text-white rounded-lg transition-all duration-200 flex items-center gap-2 mx-auto"
                                        >
                                            <Edit className="w-4 h-4" />
                                            Edit Profile
                                        </button>
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-300 mb-1">Name</label>
                                            <input
                                                type="text"
                                                value={editForm.name}
                                                onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                                                className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-chess-darker text-white focus:outline-none focus:ring-2 focus:ring-chess-gold"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-300 mb-1">Email</label>
                                            <input
                                                type="email"
                                                value={editForm.email}
                                                onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                                                className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-chess-darker text-white focus:outline-none focus:ring-2 focus:ring-chess-gold"
                                            />
                                        </div>
                                        <div className="flex space-x-2">
                                            <button
                                                onClick={handleSaveProfile}
                                                className="flex-1 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors flex items-center justify-center gap-2"
                                            >
                                                <Save className="w-4 h-4" />
                                                Save
                                            </button>
                                            <button
                                                onClick={handleCancelEdit}
                                                className="flex-1 px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-lg transition-colors flex items-center justify-center gap-2"
                                            >
                                                <X className="w-4 h-4" />
                                                Cancel
                                            </button>
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="border-t border-gray-700 pt-4">
                                <div className="flex items-center justify-between text-sm">
                                    <span className="text-gray-400">Member since</span>
                                    <span className="text-white">{formatMemberSince}</span>
                                </div>
                            </div>
                        </div>

                        {/* Quick Stats */}
                        <div className="glass-effect rounded-lg p-6">
                            <h3 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                                <Target className="w-5 h-5 text-chess-gold" />
                                Quick Stats
                            </h3>

                            <div className="space-y-3">
                                <div className="flex items-center justify-between">
                                    <span className="text-gray-400">Current Rating</span>
                                    <div className="flex items-center gap-2">
                                        <span className="text-chess-gold font-semibold">{currentRating}</span>
                                    </div>
                                </div>

                                <div className="flex items-center justify-between">
                                    <span className="text-gray-400">Win Rate</span>
                                    <span className="text-white font-semibold">{winRate}%</span>
                                </div>

                                <div className="flex items-center justify-between">
                                    <span className="text-gray-400">Total Games</span>
                                    <span className="text-white font-semibold">{totalGames}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Statistics */}
                    <div className="lg:col-span-2">
                        {/* Performance Stats */}
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                            <div className="glass-effect rounded-lg p-6 text-center">
                                <div className="flex items-center justify-center w-12 h-12 bg-chess-gold rounded-full mx-auto mb-3">
                                    <Trophy className="w-6 h-6 text-black" />
                                </div>
                                <h3 className="text-2xl font-bold text-white">{currentRating}</h3>
                                <p className="text-gray-400 text-sm">Current Rating</p>
                            </div>

                            <div className="glass-effect rounded-lg p-6 text-center">
                                <div className="flex items-center justify-center w-12 h-12 bg-green-600 rounded-full mx-auto mb-3">
                                    <TrendingUp className="w-6 h-6 text-white" />
                                </div>
                                <h3 className="text-2xl font-bold text-green-400">{wins}</h3>
                                <p className="text-gray-400 text-sm">Wins</p>
                                <div className="text-xs mt-1 text-green-400">
                                    {winRate}% win rate
                                </div>
                            </div>

                            <div className="glass-effect rounded-lg p-6 text-center">
                                <div className="flex items-center justify-center w-12 h-12 bg-yellow-600 rounded-full mx-auto mb-3">
                                    <Target className="w-6 h-6 text-white" />
                                </div>
                                <h3 className="text-2xl font-bold text-yellow-400">{draws}</h3>
                                <p className="text-gray-400 text-sm">Draws</p>
                                <div className="text-xs mt-1 text-gray-400">
                                    {totalGames > 0 ? Math.round((draws / totalGames) * 100) : 0}% draw rate
                                </div>
                            </div>

                            <div className="glass-effect rounded-lg p-6 text-center">
                                <div className="flex items-center justify-center w-12 h-12 bg-red-600 rounded-full mx-auto mb-3">
                                    <X className="w-6 h-6 text-white" />
                                </div>
                                <h3 className="text-2xl font-bold text-red-400">{losses}</h3>
                                <p className="text-gray-400 text-sm">Losses</p>
                                <div className="text-xs mt-1 text-red-400">
                                    {totalGames > 0 ? Math.round((losses / totalGames) * 100) : 0}% loss rate
                                </div>
                            </div>
                        </div>

                        {/* Recent Games */}
                        <div className="glass-effect rounded-lg p-6">
                            <div className="flex items-center justify-between mb-6">
                                <h3 className="text-xl font-semibold text-white flex items-center gap-2">
                                    <Calendar className="w-5 h-5 text-chess-gold" />
                                    Recent Games
                                </h3>
                                <button
                                    onClick={() => navigate('/history')}
                                    className="text-chess-gold hover:text-chess-gold-light transition-colors text-sm"
                                >
                                    View All →
                                </button>
                            </div>

                            {games.length === 0 ? (
                                <div className="text-center py-8">
                                    <Trophy className="w-12 h-12 text-gray-600 mx-auto mb-3" />
                                    <p className="text-gray-400">No games played yet</p>
                                    <button
                                        onClick={() => window.location.href = '/'}
                                        className="mt-4 btn-chess px-4 py-2 rounded-lg font-semibold"
                                    >
                                        Play Your First Game
                                    </button>
                                </div>
                            ) : (
                                <div className="space-y-3">
                                    {games.slice(0, 5).map((game) => {
                                        const opponent = game.whitePlayer.id === user?.id ? game.blackPlayer : game.whitePlayer;
                                        const isWhite = game.whitePlayer.id === user?.id;

                                        return (
                                            <div key={game.id} className="flex items-center justify-between p-4 bg-chess-darker rounded-lg">
                                                <div className="flex items-center space-x-4">
                                                    <div className={`text-sm font-semibold px-2 py-1 rounded ${
                                                        game.status === 'White win' && isWhite
                                                            || game.status === 'Black win' && !isWhite ? 'bg-green-900 text-green-300' :
                                                            game.status === 'White win' && !isWhite
                                                            || game.status === 'Black win' && isWhite ? 'bg-red-900 text-red-300' :
                                                                'bg-yellow-900 text-yellow-300'
                                                    }`}>
                                                        {game.status}
                                                    </div>

                                                    <div>
                                                        <p className="text-white font-medium">vs {opponent.name}</p>
                                                        <p className="text-gray-400 text-sm">
                                                            {isWhite ? 'White' : 'Black'} • {new Date(game.startTime).toLocaleDateString()}
                                                        </p>
                                                    </div>
                                                </div>

                                                <div className="text-right">
                                                    <p className="text-gray-300 text-sm">{game.status}</p>
                                                    <p className="text-gray-500 text-xs">
                                                        {game.endTime ? '15:30' : 'In Progress'} {/* Mock duration */}
                                                    </p>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Profile;