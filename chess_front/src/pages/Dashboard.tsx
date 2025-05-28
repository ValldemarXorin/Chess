import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import GameContext, { useGame } from '../context/GameContext';
import Navbar from '../components/Navbar';
import MatchmakingModal from '../components/MatchmakingModal';
import ChessBoard from '../components/ChessBoard';
import GameInfo from '../components/GameInfo';
import {GameInfoResponse, PieceColor, Position} from '../types';
import { Swords, Trophy, Users, TrendingUp } from 'lucide-react';
import {useNavigate} from "react-router-dom";
import gameContext from "../context/GameContext";
import {playersAPI} from "@/services/api";

const Dashboard: React.FC = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const {
        currentGame,
        selectedSquare,
        possibleMoves,
        makeMove,
        selectSquare,
        gameStatus,
        startGame
    } = useGame();

    const [games, setGames] = useState<GameInfoResponse[]>([]);

    const [isMatchmakingOpen, setIsMatchmakingOpen] = useState(false);

    useEffect(() => {
        const fetchGames = async () => {
            if (user) {
                try {
                    const gamesData = await playersAPI.getPlayerGames(user.id);
                    setGames(gamesData);
                    // Подсчет побед, поражений и ничьих
                    const wins = gamesData.filter(game =>
                        (game.status === "White win" && user.id === game.whitePlayer.id) ||
                        (game.status === "Black win" && user.id === game.blackPlayer.id)
                    ).length;

                    const losses = gamesData.filter(game =>
                        (game.status === "Black win" && user.id === game.whitePlayer.id) ||
                        (game.status === "White win" && user.id === game.blackPlayer.id)
                    ).length;

                    const draws = gamesData.filter(game => game.status === "Draw").length;

                    setStats({
                        gamesPlayed: gamesData.length,
                        wins,
                        losses,
                        draws,
                        rating: 1022, // Обновите по необходимости
                        onlineFriends: 1 // Обновите по необходимости
                    });
                } catch (error) {
                    console.error('Ошибка при загрузке игр:', error);
                }
            }
        };

        fetchGames();
    }, [user]);

    const [stats, setStats] = useState({
        gamesPlayed: 0,
        wins: 0,
        losses: 0,
        draws: 0,
        rating: 1022,
        onlineFriends: 1
    });

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

    const handleFindMatch = () => {
        setIsMatchmakingOpen(true);
    };

    const handleMatchFound = async (gameId: number, color: PieceColor) => {
        console.log('Match found - starting game...');
        navigate(`/game/${gameId}`);
    };

    const winRate = stats.gamesPlayed > 0 ? Math.round((stats.wins / stats.gamesPlayed) * 100) : 0;

    return (
        <div className="min-h-screen bg-chess-dark">
            <Navbar />

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Welcome Section */}
                <div className="mb-8">
                    <h1 className="text-4xl font-bold text-white mb-2">
                        Welcome back, <span className="text-chess-gold">{user?.name}</span>!
                    </h1>
                    <p className="text-gray-400 text-lg">
                        Ready for your next chess battle? Challenge worthy opponents and climb the ranks!
                    </p>
                </div>

                {/* Stats Cards */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    <div className="glass-effect rounded-lg p-6 text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-chess-gold rounded-full mx-auto mb-3">
                            <Trophy className="w-6 h-6 text-black" />
                        </div>
                        <h3 className="text-2xl font-bold text-white">{stats.rating}</h3>
                        <p className="text-gray-400 text-sm">Rating</p>
                    </div>

                    <div className="glass-effect rounded-lg p-6 text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-green-600 rounded-full mx-auto mb-3">
                            <TrendingUp className="w-6 h-6 text-white" />
                        </div>
                        <h3 className="text-2xl font-bold text-white">{winRate}%</h3>
                        <p className="text-gray-400 text-sm">Win Rate</p>
                    </div>

                    <div className="glass-effect rounded-lg p-6 text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-blue-600 rounded-full mx-auto mb-3">
                            <Swords className="w-6 h-6 text-white" />
                        </div>
                        <h3 className="text-2xl font-bold text-white">{stats.gamesPlayed}</h3>
                        <p className="text-gray-400 text-sm">Games Played</p>
                    </div>

                    <div className="glass-effect rounded-lg p-6 text-center">
                        <div className="flex items-center justify-center w-12 h-12 bg-purple-600 rounded-full mx-auto mb-3">
                            <Users className="w-6 h-6 text-white" />
                        </div>
                        <h3 className="text-2xl font-bold text-white">{stats.onlineFriends}</h3>
                        <p className="text-gray-400 text-sm">Friends Online</p>
                    </div>
                </div>

                {currentGame && gameStatus === 'in_progress' ? (
                    /* Active Game */
                    <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
                        <div className="xl:col-span-2 flex justify-center">
                            <ChessBoard onSquareClick={handleSquareClick} />
                        </div>
                        <div className="space-y-6">
                            <GameInfo />
                        </div>
                    </div>
                ) : (
                    /* No Active Game */
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Quick Play Section */}
                        <div className="glass-effect rounded-lg p-8 text-center">
                            <div className="w-20 h-20 bg-gradient-gold rounded-full flex items-center justify-center mx-auto mb-6">
                                <Swords className="w-10 h-10 text-black" />
                            </div>

                            <h2 className="text-2xl font-bold text-white mb-4">Quick Play</h2>
                            <p className="text-gray-400 mb-6">
                                Jump into a game instantly! Our matchmaking system will find you an opponent of similar skill level.
                            </p>

                            <button
                                onClick={handleFindMatch}
                                className="btn-chess px-8 py-4 rounded-lg font-semibold text-lg transition-all duration-300 hover:scale-105"
                            >
                                Find Match
                            </button>

                            <div className="mt-6 grid grid-cols-3 gap-4 text-center">
                                <div>
                                    <p className="text-chess-gold font-semibold">{stats.wins}</p>
                                    <p className="text-xs text-gray-400">Wins</p>
                                </div>
                                <div>
                                    <p className="text-red-400 font-semibold">{stats.losses}</p>
                                    <p className="text-xs text-gray-400">Losses</p>
                                </div>
                                <div>
                                    <p className="text-yellow-400 font-semibold">{stats.draws}</p>
                                    <p className="text-xs text-gray-400">Draws</p>
                                </div>
                            </div>
                        </div>

                        {/* Recent Activity */}
                        <div className="glass-effect rounded-lg p-8">
                            <h2 className="text-2xl font-bold text-white mb-6">Recent Activity</h2>
                            <div className="space-y-4">
                                {games.slice(0, 3).map(game => {
                                    const opponent = game.whitePlayer.id === user?.id ? game.blackPlayer : game.whitePlayer;
                                    const isWhite = game.whitePlayer.id === user?.id;
                                return (
                                    <div key={game.id} className="flex items-center justify-between p-4 bg-chess-darker rounded-lg">
                                        <div>
                                            <p className="text-white font-medium">
                                                {game.status === 'Draw'
                                                    ? `Draw vs. ${opponent.name}`
                                                    : (game.status === 'Black win' && isWhite || game.status === 'White win' && !isWhite
                                                            ? `Loss vs. ${opponent.name}`
                                                            : `Win vs. ${opponent.name}`
                                                    )}
                                            </p>
                                            <p className="text-gray-400 text-sm">
                                                {new Date(game.startTime).toLocaleDateString()} • {game.status}
                                            </p>
                                        </div>
                                        <div className={game.status === 'Draw' ? 'text-gray-400 font-semibold' :
                                            game.status === 'Black win' && !isWhite || game.status === 'White win' && isWhite
                                                ? 'text-green-400 font-semibold'
                                                : 'text-red-400 font-semibold'}>
                                            {game.status === 'Draw' ? 'Draw' : game.status}
                                        </div>
                                    </div>
                                );
                                })}
                            </div>
                            <button
                                onClick={() => navigate('/history')} // Добавляем обработчик
                                className="w-full mt-4 px-4 py-2 bg-chess-dark hover:bg-chess-gold hover:text-black text-white rounded-lg transition-all duration-200">
                                View All Games
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Matchmaking Modal */}
            <MatchmakingModal
                isOpen={isMatchmakingOpen}
                onClose={() => setIsMatchmakingOpen(false)}
                onMatchFound={handleMatchFound}
            />
        </div>
    );
};

export default Dashboard;