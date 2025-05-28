import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import PlayerCard from '../components/PlayerCard';
import { PlayerResponse, OnlineStatus } from '../types';
import { playersAPI } from '../services/api';
import { Search as SearchIcon, Users, UserPlus } from 'lucide-react';
import { useToast } from '../hooks/use-toast';
import webSocketService from '../services/websocket';

const Search: React.FC = () => {
    const { user } = useAuth();
    const { toast } = useToast();

    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState<PlayerResponse[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [hasSearched, setHasSearched] = useState(false);
    const [friends, setFriends] = useState<PlayerResponse[]>([]);
    const [onlineStatuses, setOnlineStatuses] = useState<{ [playerId: number]: string }>({}); // Храним статусы

    useEffect(() => {
        if (user) {
            loadFriends();
            // Подключаемся к WebSocket
            webSocketService.connect(user.id)
                .then(() => {
                    console.log('WebSocket connected in Search');
                    webSocketService.subscribeToOnlineStatus((status: OnlineStatus) => {
                        console.log('Online status update:', status);
                        setOnlineStatuses((prev) => ({
                            ...prev,
                            [status.playerId]: status.status,
                        }));
                        toast({
                            title: 'Status Update',
                            description: `Player ${status.playerId} is ${status.status}`,
                            variant: 'default',
                            duration: 3000,
                        });
                    });
                })
                .catch((error) => {
                    console.error('WebSocket connection failed:', error);
                    toast({
                        title: 'Error',
                        description: 'Failed to connect to WebSocket',
                        variant: 'destructive',
                        duration: 5000,
                    });
                });

            return () => {
                webSocketService.disconnect();
            };
        }
    }, [user, toast]);

    const loadFriends = async () => {
        if (!user) return;

        try {
            const friendsData = await playersAPI.getFriends(user.id);
            setFriends(friendsData);
        } catch (error) {
            console.error('Failed to load friends:', error);
        }
    };

    const handleSearch = async () => {
        if (!searchTerm.trim()) return;

        setIsLoading(true);
        setHasSearched(true);

        try {
            const results = await playersAPI.searchPlayers({
                name: searchTerm.includes('@') ? undefined : searchTerm,
                email: searchTerm.includes('@') ? searchTerm : undefined
            });
            const filteredResults = results.filter(player => player.id !== user?.id);
            setSearchResults(filteredResults);
        } catch (error) {
            console.error('Search failed:', error);
            toast({
                title: 'Search Failed',
                description: 'Failed to search for players. Please try again.',
                variant: 'destructive'
            });
            setSearchResults([]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSendFriendRequest = async (playerEmail: string) => {
        if (!user) return;

        try {
            await playersAPI.sendFriendRequest(user.id, playerEmail);
            toast({
                title: 'Friend Request Sent',
                description: 'Your friend request has been sent successfully!',
            });
            // Обновляем список друзей
            await loadFriends();
        } catch (error) {
            console.error('Failed to send friend request:', error);
            toast({
                title: 'Error',
                description: 'Failed to send friend request. Please try again.',
                variant: 'destructive'
            });
        }
    };

    const handleViewProfile = (playerId: number) => {
        console.log('View profile for player:', playerId);
        toast({
            title: 'Profile View',
            description: 'Profile viewing feature coming soon!',
        });
    };

    const isFriend = (playerId: number): boolean => {
        return friends.some(friend => friend.id === playerId);
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

    return (
        <div className="min-h-screen bg-chess-dark">
            <Navbar />
            <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8">
                    <h1 className="text-4xl font-bold text-white mb-2 flex items-center gap-3">
                        <SearchIcon className="w-10 h-10 text-chess-gold" />
                        Find Players
                    </h1>
                    <p className="text-gray-400 text-lg">
                        Search for chess players by name or email to expand your network
                    </p>
                </div>

                <div className="glass-effect rounded-lg p-8 mb-8">
                    <div className="max-w-2xl mx-auto">
                        <div className="flex gap-4">
                            <div className="flex-1 relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <SearchIcon className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    type="text"
                                    placeholder="Search by name or email..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    onKeyPress={handleKeyPress}
                                    className="block w-full pl-10 pr-3 py-4 border border-gray-600 rounded-lg bg-chess-darker text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-chess-gold focus:border-transparent transition-all duration-200 text-lg"
                                />
                            </div>
                            <button
                                onClick={handleSearch}
                                disabled={!searchTerm.trim() || isLoading}
                                className="btn-chess px-8 py-4 rounded-lg font-semibold text-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {isLoading ? (
                                    <div className="flex items-center">
                                        <div className="spinner mr-2"></div>
                                        Searching...
                                    </div>
                                ) : (
                                    'Search'
                                )}
                            </button>
                        </div>
                        <div className="mt-4 text-center">
                            <p className="text-gray-500 text-sm">
                                💡 Tip: You can search by full name or email address to find specific players
                            </p>
                        </div>
                    </div>
                </div>

                <div>
                    {!hasSearched ? (
                        <div className="text-center py-16">
                            <div className="w-24 h-24 bg-chess-gold bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-6">
                                <Users className="w-12 h-12 text-chess-gold" />
                            </div>
                            <h3 className="text-2xl font-semibold text-white mb-4">
                                Discover Chess Players
                            </h3>
                            <p className="text-gray-400 text-lg max-w-md mx-auto mb-8">
                                Use the search bar above to find chess players by their name or email address.
                                Build your network and challenge new opponents!
                            </p>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-3xl mx-auto">
                                <div className="glass-effect rounded-lg p-6 text-center">
                                    <UserPlus className="w-8 h-8 text-chess-gold mx-auto mb-3" />
                                    <h4 className="text-white font-semibold mb-2">Add Friends</h4>
                                    <p className="text-gray-400 text-sm">Send friend requests to connect with players</p>
                                </div>
                                <div className="glass-effect rounded-lg p-6 text-center">
                                    <SearchIcon className="w-8 h-8 text-chess-gold mx-auto mb-3" />
                                    <h4 className="text-white font-semibold mb-2">Quick Search</h4>
                                    <p className="text-gray-400 text-sm">Find players instantly by name or email</p>
                                </div>
                                <div className="glass-effect rounded-lg p-6 text-center">
                                    <Users className="w-8 h-8 text-chess-gold mx-auto mb-3" />
                                    <h4 className="text-white font-semibold mb-2">View Profiles</h4>
                                    <p className="text-gray-400 text-sm">Check out player stats and game history</p>
                                </div>
                            </div>
                        </div>
                    ) : searchResults.length === 0 ? (
                        <div className="text-center py-16">
                            <SearchIcon className="w-16 h-16 text-gray-600 mx-auto mb-4" />
                            <h3 className="text-xl font-semibold text-gray-400 mb-2">
                                No players found
                            </h3>
                            <p className="text-gray-500 mb-6">
                                We couldn't find any players matching "{searchTerm}". Try adjusting your search terms.
                            </p>
                            <div className="max-w-md mx-auto text-left">
                                <h4 className="text-chess-gold font-semibold mb-2">Search Tips:</h4>
                                <ul className="text-gray-400 text-sm space-y-1">
                                    <li>• Try searching with a partial name</li>
                                    <li>• Use the complete email address</li>
                                    <li>• Check for typos in your search</li>
                                    <li>• Try different variations of the name</li>
                                </ul>
                            </div>
                        </div>
                    ) : (
                        <div>
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="text-2xl font-semibold text-white">
                                    Search Results ({searchResults.length})
                                </h2>
                                <p className="text-gray-400">
                                    Found {searchResults.length} player{searchResults.length !== 1 ? 's' : ''} for "{searchTerm}"
                                </p>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {searchResults.map((player) => (
                                    <PlayerCard
                                        key={player.id}
                                        player={player}
                                        isOnline={onlineStatuses[player.id] === 'ONLINE'} // Реальный статус
                                        isFriend={isFriend(player.id)}
                                        canSendFriendRequest={!isFriend(player.id)}
                                        onSendFriendRequest={() => handleSendFriendRequest(player.email)}
                                        onViewProfile={handleViewProfile}
                                        showActions={true}
                                    />
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Search;