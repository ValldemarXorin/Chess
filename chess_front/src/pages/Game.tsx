import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useGame } from '../context/GameContext';
import Navbar from '../components/Navbar';
import ChessBoard from '../components/ChessBoard';
import GameInfo from '../components/GameInfo';
import { Position } from '../types';
import { ArrowLeft } from 'lucide-react';

const Game: React.FC = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const {
        currentGame,
        selectedSquare,
        possibleMoves,
        makeMove,
        selectSquare,
        gameStatus,
        startGame,
        myColor
    } = useGame();

    // Redirect to dashboard if no active game
    useEffect(() => {
        if (!currentGame && gameStatus !== 'in_progress') {
            navigate('/');
        }
    }, [currentGame, gameStatus, navigate]);

    const handleSquareClick = (position: Position) => {
        if (selectedSquare) {
            // If a square is already selected, try to make a move
            const isValidMove = possibleMoves.some(
                move => move.x === position.x && move.y === position.y
            );

            if (isValidMove) {
                makeMove(selectedSquare, position);
            } else {
                // Select the new square if it has a piece
                selectSquare(position);
            }
        } else {
            // Select the square
            selectSquare(position);
        }
    };

    const handleBackToDashboard = () => {
        navigate('/');
    };

    if (!currentGame) {
        return (
            <div className="min-h-screen bg-chess-dark">
                <Navbar />
                <div className="flex items-center justify-center h-96">
                    <div className="text-center">
                        <div className="spinner mx-auto mb-4"></div>
                        <p className="text-chess-gold">Loading game...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-chess-dark">
            <Navbar />

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Header */}
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h1 className="text-4xl font-bold text-white mb-2">
                            Chess Game
                        </h1>
                        <p className="text-gray-400 text-lg">
                            Show your chess mastery and defeat your opponent!
                        </p>
                    </div>

                    <button
                        onClick={handleBackToDashboard}
                        className="flex items-center gap-2 px-4 py-2 bg-chess-dark hover:bg-chess-gold hover:text-black text-white rounded-lg transition-all duration-200"
                    >
                        <ArrowLeft className="w-4 h-4" />
                        Back to Dashboard
                    </button>
                </div>

                {/* Game Layout */}
                <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
                    {/* Chess Board */}
                    <div className="xl:col-span-2 flex justify-center">
                        <ChessBoard onSquareClick={handleSquareClick} />
                    </div>

                    {/* Game Information */}
                    <div className="space-y-6">
                        <GameInfo />

                        {/* Game ended actions */}
                        {(gameStatus === 'checkmate' || gameStatus === 'stalemate' || gameStatus === 'draw' || gameStatus === 'resigned') && (
                            <div className="glass-effect rounded-lg p-6 space-y-4">
                                <h3 className="text-xl font-bold text-chess-gold text-center">Game Over</h3>
                                <div className="space-y-2">
                                    <button
                                        onClick={handleBackToDashboard}
                                        className="w-full px-4 py-3 bg-chess-gold hover:bg-chess-gold-light text-black rounded-lg font-semibold transition-all duration-200"
                                    >
                                        Return to Dashboard
                                    </button>
                                    <button
                                        onClick={() => navigate('/search')}
                                        className="w-full px-4 py-2 bg-chess-dark hover:bg-chess-gold hover:text-black text-white rounded-lg transition-all duration-200"
                                    >
                                        Find New Match
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Game;