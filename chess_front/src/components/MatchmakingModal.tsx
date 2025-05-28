import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useGame } from '../context/GameContext';
import webSocketService from '../services/websocket';
import { MatchFoundResponse, PieceColor } from '../types';
import { Loader2, Search, X } from 'lucide-react';
import { matchmakingAPI } from '../services/api';
import {useNavigate} from "react-router-dom";

interface MatchmakingModalProps {
    isOpen: boolean;
    onClose: () => void;
    onMatchFound: (gameId: number, color: PieceColor) => void;
}

const MatchmakingModal: React.FC<MatchmakingModalProps> = ({ isOpen, onClose, onMatchFound }) => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const { startGame } = useGame();
    const [isSearching, setIsSearching] = useState(false);
    const [searchTime, setSearchTime] = useState(0);
    const [estimatedWaitTime, setEstimatedWaitTime] = useState(30);

    useEffect(() => {
        let interval: number;

        if (isSearching) {
            interval = window
                .setInterval(() => {
                setSearchTime(prev => prev + 1);
            }, 1000);
        }

        return () => {
            if (interval) window.clearInterval(interval);
        };
    }, [isSearching]);

    useEffect(() => {
        if (isOpen && user) {
            // Subscribe to matchmaking updates
            webSocketService.subscribeToMatchmaking(user.id, (response: MatchFoundResponse) => {
                console.log('Match found:', response);
                setIsSearching(false);

                // Start the game
                console.log(47);
                const myColor = response.color as PieceColor;
                console.log(49);
                startGame(response.gameId, myColor);
                console.log(51);
                onMatchFound(response.gameId, myColor);
                console.log(53);
                onClose();
                console.log('Navigating to:', `/game/${response.gameId}`);
                navigate(`/game/${response.gameId}`);
                console.log('Navigation called');
            });

            return () => {
                if (user) {
                    webSocketService.unsubscribe(`matchmaking-user-${user.id}`);
                }
            };
        }
    }, [isOpen, user, startGame, onMatchFound, onClose]);

    const handleStartSearch = () => {
        if (!user) return;

        setIsSearching(true);
        setSearchTime(0);

        // Join matchmaking pool via WebSocket
        webSocketService.joinMatchmakingPool(user.id);
        console.log('Joining matchmaking pool for user:', user.id);
    };

    const handleCancelSearch = async () => {
        if (!user) return;

        setIsSearching(false);
        setSearchTime(0);

        try {
            // Cancel matchmaking (using placeholder endpoint)
            await matchmakingAPI.cancelMatchmaking(user.id);
            console.log('Cancelled matchmaking for user:', user.id);
        } catch (error) {
            console.error('Failed to cancel matchmaking:', error);
        }
    };

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const getSearchAnimation = () => {
        if (!isSearching) return '';
        return 'animate-pulse';
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="glass-effect rounded-lg max-w-md w-full p-6 animate-fade-in-up">
                {/* Header */}
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-2xl font-bold text-chess-gold flex items-center gap-2">
                        <Search className="w-6 h-6" />
                        Matchmaking
                    </h2>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-white transition-colors"
                        disabled={isSearching}
                    >
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {!isSearching ? (
                    /* Start Search */
                    <div className="text-center space-y-6">
                        <div className="bg-chess-dark rounded-lg p-6 border border-chess-gold border-opacity-20">
                            <div className="w-20 h-20 mx-auto mb-4 bg-gradient-gold rounded-full flex items-center justify-center">
                                <Search className="w-10 h-10 text-black" />
                            </div>
                            <h3 className="text-xl font-semibold text-white mb-2">Find an Opponent</h3>
                            <p className="text-gray-400 text-sm">
                                Click below to join the matchmaking queue and find a worthy opponent for an epic chess battle!
                            </p>
                        </div>

                        <div className="space-y-4">
                            <div className="flex items-center justify-between text-sm">
                                <span className="text-gray-400">Estimated wait time:</span>
                                <span className="text-chess-gold font-semibold">{estimatedWaitTime}s</span>
                            </div>

                            <button
                                onClick={handleStartSearch}
                                className="w-full btn-chess py-3 rounded-lg font-semibold text-lg transition-all duration-300 hover:scale-105"
                            >
                                Find Match
                            </button>
                        </div>
                    </div>
                ) : (
                    /* Searching */
                    <div className="text-center space-y-6">
                        <div className="bg-chess-dark rounded-lg p-6 border border-chess-gold border-opacity-20">
                            <div className={`w-20 h-20 mx-auto mb-4 bg-gradient-gold rounded-full flex items-center justify-center ${getSearchAnimation()}`}>
                                <Loader2 className="w-10 h-10 text-black animate-spin" />
                            </div>
                            <h3 className="text-xl font-semibold text-white mb-2">Searching for Opponent...</h3>
                            <p className="text-gray-400 text-sm">
                                Please wait while we find you a suitable opponent. This may take a few moments.
                            </p>
                        </div>

                        <div className="space-y-4">
                            <div className="bg-chess-darker rounded-lg p-4 space-y-2">
                                <div className="flex items-center justify-between">
                                    <span className="text-gray-400">Search time:</span>
                                    <span className="text-chess-gold font-mono text-lg">{formatTime(searchTime)}</span>
                                </div>

                                <div className="w-full bg-gray-700 rounded-full h-2">
                                    <div
                                        className="bg-gradient-gold h-2 rounded-full transition-all duration-1000 animate-pulse-gold"
                                        style={{ width: `${Math.min((searchTime / estimatedWaitTime) * 100, 100)}%` }}
                                    ></div>
                                </div>

                                <div className="flex items-center justify-between text-xs text-gray-500">
                                    <span>Looking for players...</span>
                                    <span>{Math.max(estimatedWaitTime - searchTime, 0)}s remaining</span>
                                </div>
                            </div>

                            <button
                                onClick={handleCancelSearch}
                                className="w-full px-4 py-3 bg-red-600 hover:bg-red-700 text-white rounded-lg font-semibold transition-all duration-300"
                            >
                                Cancel Search
                            </button>
                        </div>
                    </div>
                )}

                {/* Tips */}
                <div className="mt-6 p-4 bg-chess-darker rounded-lg border-l-4 border-chess-gold">
                    <h4 className="text-sm font-semibold text-chess-gold mb-1">💡 Pro Tip</h4>
                    <p className="text-xs text-gray-400">
                        While waiting, you can review your previous games or check out your friends' activities!
                    </p>
                </div>
            </div>
        </div>
    );
};

export default MatchmakingModal;