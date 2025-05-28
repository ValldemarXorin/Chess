import React from 'react';
import { useGame } from '../context/GameContext';
import { useAuth } from '../context/AuthContext';
import { PieceColor } from '../types';
import { Clock, Crown, Users } from 'lucide-react';

const GameInfo: React.FC = () => {
    const { gameStatus, currentTurn, isMyTurn, myColor, gameHistory } = useGame();
    const { user } = useAuth();

    const getStatusMessage = (): string => {
        switch (gameStatus) {
            case 'waiting':
                return 'Waiting for game to start...';
            case 'in_progress':
                return isMyTurn ? 'Your turn' : 'Opponent\'s turn';
            case 'checkmate':
                return `Checkmate! ${currentTurn === myColor ? 'You lose' : 'You win'}`;
            case 'stalemate':
                return 'Stalemate - Draw';
            case 'draw':
                return 'Game ended in a draw';
            case 'resigned':
                return 'Game ended by resignation';
            default:
                return 'Game status unknown';
        }
    };

    const getStatusColor = (): string => {
        if (gameStatus === 'checkmate') {
            return currentTurn === myColor ? 'text-red-400' : 'text-green-400';
        }
        if (gameStatus === 'in_progress') {
            return isMyTurn ? 'text-chess-gold' : 'text-gray-400';
        }
        return 'text-gray-400';
    };

    return (
        <div className="glass-effect rounded-lg p-6 space-y-4">
            {/* Game Status */}
            <div className="text-center">
                <h2 className="text-2xl font-bold text-chess-gold mb-2 flex items-center justify-center gap-2">
                    <Crown className="w-6 h-6" />
                    Chess Arena
                </h2>
                <p className={`text-lg font-semibold ${getStatusColor()}`}>
                    {getStatusMessage()}
                </p>
            </div>

            {/* Player Info */}
            {myColor && (
                <div className="space-y-3">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <Users className="w-4 h-4 text-chess-gold" />
                            <span className="text-sm text-gray-300">You are playing as:</span>
                        </div>
                        <div className={`px-3 py-1 rounded-full text-sm font-semibold ${
                            myColor === PieceColor.WHITE
                                ? 'bg-gray-100 text-gray-800'
                                : 'bg-gray-800 text-gray-100'
                        }`}>
                            {myColor === PieceColor.WHITE ? '♔ White' : '♛ Black'}
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <Clock className="w-4 h-4 text-chess-gold" />
                            <span className="text-sm text-gray-300">Current turn:</span>
                        </div>
                        <div className={`px-3 py-1 rounded-full text-sm font-semibold ${
                            currentTurn === PieceColor.WHITE
                                ? 'bg-gray-100 text-gray-800'
                                : 'bg-gray-800 text-gray-100'
                        }`}>
                            {currentTurn === PieceColor.WHITE ? '♔ White' : '♛ Black'}
                        </div>
                    </div>
                </div>
            )}

            {/* Turn Indicator */}
            {gameStatus === 'in_progress' && (
                <div className="relative">
                    <div className={`w-full h-2 rounded-full transition-all duration-500 ${
                        isMyTurn
                            ? 'bg-gradient-to-r from-chess-gold to-chess-gold-light animate-pulse-gold'
                            : 'bg-gray-600'
                    }`}>
                        {isMyTurn && (
                            <div className="absolute inset-0 rounded-full bg-chess-gold animate-pulse"></div>
                        )}
                    </div>
                    <p className="text-xs text-center mt-1 text-gray-400">
                        {isMyTurn ? 'Make your move!' : 'Waiting for opponent...'}
                    </p>
                </div>
            )}

            {/* Move Counter */}
            <div className="text-center">
        <span className="text-sm text-gray-400">
          Moves played: <span className="text-chess-gold font-semibold">{gameHistory.length}</span>
        </span>
            </div>

            {/* Game Controls */}
            <div className="space-y-2">
                <button
                    className="w-full px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded-lg transition-colors duration-200"
                    disabled={gameStatus !== 'in_progress'}
                >
                    Offer Draw
                </button>
                <button
                    className="w-full px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors duration-200"
                    disabled={gameStatus !== 'in_progress'}
                >
                    Resign
                </button>
            </div>
        </div>
    );
};

export default GameInfo;