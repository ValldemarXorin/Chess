import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import PlayerCard from '../components/PlayerCard';
import { PlayerResponse, FriendRequest } from '../types';
import { playersAPI } from '../services/api';
import { Users, UserPlus, Inbox, Search } from 'lucide-react';
import { useToast } from '../hooks/use-toast';

const Friends: React.FC = () => {
    const { user } = useAuth();
    const { toast } = useToast();

    const [activeTab, setActiveTab] = useState<'friends' | 'requests'>('friends');
    const [friends, setFriends] = useState<PlayerResponse[]>([]);
    const [friendRequests, setFriendRequests] = useState<PlayerResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        if (user) {
            loadFriendsData();
        }
    }, [user]);

    const loadFriendsData = async () => {
        if (!user) return;

        setIsLoading(true);
        try {
            const [friendsData, requestsData] = await Promise.all([
                playersAPI.getFriends(user.id),
                playersAPI.getFriendRequests(user.id)
            ]);

            setFriends(friendsData);
            setFriendRequests(requestsData);
        } catch (error) {
            console.error('Failed to load friends data:', error);
            toast({
                title: "Error",
                description: "Failed to load friends data. Please try again.",
                variant: "destructive"
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleRemoveFriend = async (friendId: number, friendEmail: string) => {
        if (!user) return;

        try {
            await playersAPI.removeFriend(user.id, friendEmail);
            setFriends(friends.filter(friend => friend.id !== friendId));
            toast({
                title: "Friend Removed",
                description: "Friend has been removed from your friends list.",
            });
        } catch (error) {
            console.error('Failed to remove friend:', error);
            toast({
                title: "Error",
                description: "Failed to remove friend. Please try again.",
                variant: "destructive"
            });
        }
    };

    const handleApproveRequest = async (senderId: number, recipientEmail: string) => {
        try {
            await playersAPI.approveFriendRequest(senderId, recipientEmail);
            await loadFriendsData(); // Reload data
            toast({
                title: "Friend Request Approved",
                description: "You are now friends!",
            });
        } catch (error) {
            console.error('Failed to approve friend request:', error);
            toast({
                title: "Error",
                description: "Failed to approve friend request. Please try again.",
                variant: "destructive"
            });
        }
    };

    const handleRejectRequest = async (requestId: number) => {
        try {
            await playersAPI.rejectFriendRequest(requestId);
            setFriendRequests(friendRequests.filter(req => req.id !== requestId));
            toast({
                title: "Friend Request Rejected",
                description: "Friend request has been rejected.",
            });
        } catch (error) {
            console.error('Failed to reject friend request:', error);
            toast({
                title: "Error",
                description: "Failed to reject friend request. Please try again.",
                variant: "destructive"
            });
        }
    };

    const filteredFriends = friends.filter(friend =>
        friend &&
        typeof friend.name === 'string' &&
        typeof friend.email === 'string' &&
        (friend.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            friend.email.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    const filteredRequests = friendRequests.filter(request =>
        request &&
        typeof request.name === 'string' &&
        typeof request.email === 'string' &&
        (request.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            request.email.toLowerCase().includes(searchTerm.toLowerCase()))
    )

    if (isLoading) {
        return (
            <div className="min-h-screen bg-chess-dark">
                <Navbar />
                <div className="flex items-center justify-center h-96">
                    <div className="text-center">
                        <div className="spinner mx-auto mb-4"></div>
                        <p className="text-chess-gold">Loading friends...</p>
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
                        <Users className="w-10 h-10 text-chess-gold" />
                        Friends
                    </h1>
                    <p className="text-gray-400 text-lg">
                        Manage your friends and connect with other chess players
                    </p>
                </div>

                {/* Tabs */}
                <div className="flex space-x-1 bg-chess-darker rounded-lg p-1 mb-8 w-fit">
                    <button
                        onClick={() => setActiveTab('friends')}
                        className={`flex items-center gap-2 px-6 py-3 rounded-md font-medium transition-all duration-200 ${
                            activeTab === 'friends'
                                ? 'bg-chess-gold text-black'
                                : 'text-gray-300 hover:text-white hover:bg-chess-dark'
                        }`}
                    >
                        <Users className="w-4 h-4" />
                        Friends ({friends.length})
                    </button>
                    <button
                        onClick={() => setActiveTab('requests')}
                        className={`flex items-center gap-2 px-6 py-3 rounded-md font-medium transition-all duration-200 ${
                            activeTab === 'requests'
                                ? 'bg-chess-gold text-black'
                                : 'text-gray-300 hover:text-white hover:bg-chess-dark'
                        }`}
                    >
                        <Inbox className="w-4 h-4" />
                        Requests ({friendRequests.length})
                    </button>
                </div>

                {/* Search */}
                <div className="relative mb-6">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Search className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                        type="text"
                        placeholder={`Search ${activeTab}...`}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="block w-full pl-10 pr-3 py-3 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200"
                    />
                </div>

                {/* Content */}
                {activeTab === 'friends' ? (
                    <div>
                        {filteredFriends.length === 0 ? (
                            <div className="text-center py-12">
                                <Users className="w-16 h-16 text-gray-600 mx-auto mb-4" />
                                <h3 className="text-xl font-semibold text-gray-400 mb-2">
                                    {searchTerm ? 'No friends found' : 'No friends yet'}
                                </h3>
                                <p className="text-gray-500 mb-6">
                                    {searchTerm
                                        ? 'Try adjusting your search terms'
                                        : 'Start connecting with other chess players to build your network!'
                                    }
                                </p>
                                {!searchTerm && (
                                    <button
                                        onClick={() => window.location.href = '/search'}
                                        className="btn-chess px-6 py-3 rounded-lg font-semibold"
                                    >
                                        <UserPlus className="w-4 h-4 mr-2 inline" />
                                        Find Players
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {filteredFriends.map((friend) => (
                                    <div key={friend.id} className="glass-effect rounded-lg p-6">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center space-x-4">
                                                <PlayerCard
                                                    player={friend}
                                                    isOnline={Math.random() > 0.5} // Замените на реальный статус
                                                    showActions={false}
                                                    compact={true}
                                                />
                                            </div>
                                            <div className="flex space-x-3">
                                                <button
                                                    onClick={() => handleRemoveFriend(friend.id, friend.email)}
                                                    className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors duration-200"
                                                >
                                                    Remove
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ) : (
                    <div>
                        {filteredRequests.length === 0 ? (
                            <div className="text-center py-12">
                                <Inbox className="w-16 h-16 text-gray-600 mx-auto mb-4" />
                                <h3 className="text-xl font-semibold text-gray-400 mb-2">
                                    {searchTerm ? 'No requests found' : 'No friend requests'}
                                </h3>
                                <p className="text-gray-500">
                                    {searchTerm
                                        ? 'Try adjusting your search terms'
                                        : 'You don\'t have any pending friend requests at the moment.'
                                    }
                                </p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {filteredRequests.map((request) => (
                                    <div key={request.id} className="glass-effect rounded-lg p-6">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center space-x-4">
                                                <PlayerCard
                                                    player={request}
                                                    isOnline={Math.random() > 0.5} // Замените на реальный статус
                                                    showActions={false}
                                                    compact={true}
                                                />
                                            </div>
                                            <div className="flex space-x-3">
                                                <button
                                                    onClick={() => {
                                                        if (!user || !user.email) {
                                                            toast({
                                                                title: "Ошибка",
                                                                description: "Пожалуйста, войдите в систему.",
                                                                variant: "destructive"
                                                            });
                                                            return;
                                                        }
                                                        handleApproveRequest(request.id, user.email);
                                                    }}
                                                    className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors duration-200 flex items-center gap-2"
                                                >
                                                    <UserPlus className="w-4 h-4" />
                                                    Accept
                                                </button>
                                                <button
                                                    onClick={() => handleRejectRequest(request.id)}
                                                    className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors duration-200"
                                                >
                                                    Decline
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Friends;