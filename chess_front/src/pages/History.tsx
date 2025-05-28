import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import { GameInfoResponse } from '../types';
import { playersAPI, gamesAPI } from '../services/api';
import { History as HistoryIcon, Trophy, Clock, User, Eye } from 'lucide-react';
import { useToast } from '../hooks/use-toast';

const History: React.FC = () => {
    const { user } = useAuth();
    const { toast } = useToast();

    const [games, setGames] = useState<GameInfoResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedGame, setSelectedGame] = useState<GameInfoResponse | null>(null);

    useEffect(() => {
        if (user) {
            loadGameHistory();
        }
    }, [user]);

    const loadGameHistory = async () => {
        if (!user) return;

        setIsLoading(true);
        try {
            const gamesData = await playersAPI.getPlayerGames(user.id);
            setGames(gamesData);
        } catch (error) {
            console.error('Failed to load game history:', error);
            toast({
                title: "Error",
                description: "Failed to load game history. Please try again.",
                variant: "destructive"
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleViewGame = async (gameId: number) => {
        try {
            const gameDetails = await gamesAPI.getGame(gameId);
            setSelectedGame(gameDetails);
        } catch (error) {
            console.error('Failed to load game details:', error);
            toast({
                title: "Error",
                description: "Failed to load game details. Please try again.",
                variant: "destructive"
            });
        }
    };

    const getGameResult = (game: GameInfoResponse): { result: string; color: string } => {
        if (!user) return { result: 'Unknown', color: 'text-gray-400' };

        // Проверяем, что status существует и не null/undefined
        const status = game.status?.toLowerCase() || '';

        switch (status) {
            case 'white win':
                const isWhiteWinner = (game.whitePlayer.id === user.id);
                return {
                    result: isWhiteWinner ? 'Win' : 'Loss',
                    color: isWhiteWinner ? 'text-green-400' : 'text-red-400'
                };
            case 'black win':
                const isBlackWinner = (game.blackPlayer.id === user.id);
                return {
                    result: isBlackWinner ? 'Win' : 'Loss',
                    color: isBlackWinner ? 'text-green-400' : 'text-red-400'
                };
            case 'draw':
                return { result: 'Draw', color: 'text-yellow-400' };
            default:
                return { result: status || 'Unknown', color: 'text-gray-400' };
        }
    };

    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const calculateGameDuration = (startTime: string, endTime: string): string => {
        const start = new Date(startTime);
        const end = new Date(endTime);
        const durationMs = end.getTime() - start.getTime();
        const minutes = Math.floor(durationMs / (1000 * 60));
        const seconds = Math.floor((durationMs % (1000 * 60)) / 1000);
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-chess-dark">
                <Navbar />
                <div className="flex items-center justify-center h-96">
                    <div className="text-center">
                        <div className="spinner mx-auto mb-4"></div>
                        <p className="text-chess-gold">Loading game history...</p>
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
                        <HistoryIcon className="w-10 h-10 text-chess-gold" />
                        Game History
                    </h1>
                    <p className="text-gray-400 text-lg">
                        Review your past games and analyze your chess journey
                    </p>
                </div>

                {/* Stats Summary */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    <div className="glass-effect rounded-lg p-6 text-center">
                        <h3 className="text-2xl font-bold text-white">{games.length}</h3>
                        <p className="text-gray-400 text-sm">Total Games</p>
                    </div>

                    <div className="glass-effect rounded-lg p-6 text-center">
                        <h3 className="text-2xl font-bold text-green-400">
                            {games.filter(game => getGameResult(game).result === 'Win').length}
                        </h3>
                        <p className="text-gray-400 text-sm">Wins</p>
                    </div>

                    <div className="glass-effect rounded-lg p-6 text-center">
                        <h3 className="text-2xl font-bold text-yellow-400">
                            {games.filter(game => getGameResult(game).result === 'Draw').length}
                        </h3>
                        <p className="text-gray-400 text-sm">Draws</p>
                    </div>

                    <div className="glass-effect rounded-lg p-6 text-center">
                        <h3 className="text-2xl font-bold text-red-400">
                            {games.filter(game => getGameResult(game).result === 'Loss').length}
                        </h3>
                        <p className="text-gray-400 text-sm">Losses</p>
                    </div>
                </div>

                {/* Games List */}
                {games.length === 0 ? (
                    <div className="text-center py-16">
                        <HistoryIcon className="w-16 h-16 text-gray-600 mx-auto mb-4" />
                        <h3 className="text-xl font-semibold text-gray-400 mb-2">
                            No games played yet
                        </h3>
                        <p className="text-gray-500 mb-6">
                            Start playing chess games to build your game history!
                        </p>
                        <button
                            onClick={() => window.location.href = '/'}
                            className="btn-chess px-6 py-3 rounded-lg font-semibold"
                        >
                            <Trophy className="w-4 h-4 mr-2 inline" />
                            Play Your First Game
                        </button>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {games.map((game) => {
                            const opponent = game.whitePlayer.id === user?.id ? game.blackPlayer : game.whitePlayer;
                            const isWhite = game.whitePlayer.id === user?.id;
                            const result = getGameResult(game);

                            return (
                                <div key={game.id} className="glass-effect rounded-lg p-6 hover:border-chess-gold hover:border-opacity-40 transition-all duration-300">
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center space-x-6">
                                            {/* Game Result */}
                                            <div className="text-center">
                                                <div className={`text-2xl font-bold ${result.color}`}>
                                                    {result.result}
                                                </div>
                                                <div className="text-xs text-gray-400">
                                                    {isWhite ? 'White' : 'Black'}
                                                </div>
                                            </div>

                                            {/* Opponent Info */}
                                            <div className="flex items-center space-x-3">
                                                <div className="w-12 h-12 bg-chess-gold rounded-full flex items-center justify-center">
                                                    <User className="w-6 h-6 text-black" />
                                                </div>
                                                <div>
                                                    <h3 className="text-lg font-semibold text-white">
                                                        vs {opponent.name}
                                                    </h3>
                                                    <p className="text-gray-400 text-sm">{opponent.email}</p>
                                                </div>
                                            </div>

                                            {/* Game Info */}
                                            <div className="space-y-1">
                                                <div className="flex items-center text-gray-300 text-sm">
                                                    <Clock className="w-4 h-4 mr-2" />
                                                    {game.endTime ? calculateGameDuration(game.startTime, game.endTime) : 'In Progress'}
                                                </div>
                                                <div className="text-gray-400 text-xs">
                                                    {formatDate(game.startTime)}
                                                </div>
                                            </div>
                                        </div>

                                        {/* Actions */}
                                        <div className="flex items-center space-x-3">
                      <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          game.status === 'completed'
                              ? 'bg-green-900 text-green-300'
                              : 'bg-yellow-900 text-yellow-300'
                      }`}>
                        {game.status}
                      </span>

                                            <button
                                                onClick={() => handleViewGame(game.id)}
                                                className="px-4 py-2 bg-chess-dark hover:bg-chess-gold hover:text-black text-white rounded-lg transition-all duration-200 flex items-center gap-2"
                                            >
                                                <Eye className="w-4 h-4" />
                                                View
                                            </button>
                                        </div>
                                    </div>

                                    {/* Game Notes */}
                                    {game.notes ? (
                                        <div className="mt-4 p-3 bg-chess-darker rounded-lg">
                                            <p className="text-gray-300 text-sm italic">{game.notes}</p>
                                        </div>
                                    ) : null}
                                </div>
                            );
                        })}
                    </div>
                )}

                {/* Game Details Modal */}
                {selectedGame && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                        <div className="glass-effect rounded-lg max-w-2xl w-full p-6 animate-fade-in-up max-h-[90vh] overflow-y-auto">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="text-2xl font-bold text-chess-gold">Game Details</h2>
                                <button
                                    onClick={() => setSelectedGame(null)}
                                    className="text-gray-400 hover:text-white transition-colors"
                                >
                                    ×
                                </button>
                            </div>

                            <div className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="bg-chess-darker rounded-lg p-4">
                                        <h3 className="text-white font-semibold mb-2">White Player</h3>
                                        <p className="text-gray-300">{selectedGame.whitePlayer.name}</p>
                                        <p className="text-gray-400 text-sm">{selectedGame.whitePlayer.email}</p>
                                    </div>

                                    <div className="bg-chess-darker rounded-lg p-4">
                                        <h3 className="text-white font-semibold mb-2">Black Player</h3>
                                        <p className="text-gray-300">{selectedGame.blackPlayer.name}</p>
                                        <p className="text-gray-400 text-sm">{selectedGame.blackPlayer.email}</p>
                                    </div>
                                </div>

                                <div className="bg-chess-darker rounded-lg p-4">
                                    <h3 className="text-white font-semibold mb-2">Game Information</h3>
                                    <div className="grid grid-cols-2 gap-4 text-sm">
                                        <div>
                                            <span className="text-gray-400">Started:</span>
                                            <span className="text-white ml-2">{formatDate(selectedGame.startTime)}</span>
                                        </div>
                                        <div>
                                            <span className="text-gray-400">Ended:</span>
                                            <span className="text-white ml-2">
                        {selectedGame.endTime ? formatDate(selectedGame.endTime) : 'In Progress'}
                      </span>
                                        </div>
                                        <div>
                                            <span className="text-gray-400">Status:</span>
                                            <span className="text-white ml-2">{selectedGame.status}</span>
                                        </div>
                                        <div>
                                            <span className="text-gray-400">Duration:</span>
                                            <span className="text-white ml-2">
                        {selectedGame.endTime ? calculateGameDuration(selectedGame.startTime, selectedGame.endTime) : 'Ongoing'}
                      </span>
                                        </div>
                                    </div>
                                </div>

                                {selectedGame.notes && (
                                    <div className="bg-chess-darker rounded-lg p-4">
                                        <h3 className="text-white font-semibold mb-2">Notes</h3>
                                        <p className="text-gray-300">{selectedGame.notes}</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default History;