import React, { useState, useEffect } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { FaChessBoard } from 'react-icons/fa';
import { subscribe, unsubscribe, sendMessage } from '../services/socket';
import '../index.css';

const Game: React.FC = () => {
    const { gameId } = useParams<{ gameId: string }>();
    const location = useLocation();
    const { color, playerId } = location.state || { color: 'spectator', playerId: null };
    const [gameState, setGameState] = useState({
        board: initializeBoard(),
        status: 'Игра началась',
        currentTurnColor: 'WHITE' as 'WHITE' | 'BLACK',
        selectedSquare: null as [number, number] | null,
    });
    const [message, setMessage] = useState<string>('');

    // Инициализация начального состояния доски
    function initializeBoard() {
        const board: { color: 'WHITE' | 'BLACK'; type: string }[][] = Array(8).fill(null).map(() => Array(8).fill(null));

        // Расстановка фигур (стандартная начальная позиция)
        // 1-й и 8-й ряды: основные фигуры
        board[0] = [
            { color: 'BLACK', type: 'ROOK' }, { color: 'BLACK', type: 'KNIGHT' }, { color: 'BLACK', type: 'BISHOP' },
            { color: 'BLACK', type: 'QUEEN' }, { color: 'BLACK', type: 'KING' }, { color: 'BLACK', type: 'BISHOP' },
            { color: 'BLACK', type: 'KNIGHT' }, { color: 'BLACK', type: 'ROOK' },
        ];
        board[7] = [
            { color: 'WHITE', type: 'ROOK' }, { color: 'WHITE', type: 'KNIGHT' }, { color: 'WHITE', type: 'BISHOP' },
            { color: 'WHITE', type: 'QUEEN' }, { color: 'WHITE', type: 'KING' }, { color: 'WHITE', type: 'BISHOP' },
            { color: 'WHITE', type: 'KNIGHT' }, { color: 'WHITE', type: 'ROOK' },
        ];

        // 2-й и 7-й ряды: пешки
        board[1].fill({ color: 'BLACK', type: 'PAWN' });
        board[6].fill({ color: 'WHITE', type: 'PAWN' });

        return board;
    }

    // Подписка на обновления хода
    useEffect(() => {
        if (!gameId || !playerId) {
            console.warn('Недостаточно данных для игры:', { gameId, playerId });
            return;
        }

        const destination = `/game/${gameId}/move`;
        const subscriptionId = subscribe(destination, (message: any) => {
            console.log('Обновление состояния игры:', message.body);
            try {
                const updatedState = JSON.parse(message.body);
                setGameState(prevState => ({
                    ...prevState,
                    board: updatedState.board || prevState.board,
                    status: updatedState.status || prevState.status,
                    currentTurnColor: updatedState.currentTurnColor || prevState.currentTurnColor,
                }));
            } catch (e) {
                console.error('Ошибка парсинга обновления игры:', e);
            }
        });

        return () => {
            if (subscriptionId) {
                unsubscribe(subscriptionId);
                console.log('Отписка от обновлений игры');
            }
        };
    }, [gameId, playerId]);

    const handleSquareClick = (x: number, y: number) => {
        if (!playerId) return;

        if (gameState.selectedSquare) {
            const [startX, startY] = gameState.selectedSquare;
            const move = {
                playerId,
                startX,
                startY,
                endX: x,
                endY: y,
            };
            sendMessage(`/app/game/${gameId}/move`, move);
            setGameState(prev => ({ ...prev, selectedSquare: null }));
            setMessage('Ход отправлен, ждите ответа сервера...');
        } else if (gameState.board[x][y] && (color === 'white' && gameState.currentTurnColor === 'WHITE' || color === 'black' && gameState.currentTurnColor === 'BLACK')) {
            setGameState(prev => ({ ...prev, selectedSquare: [x, y] }));
            setMessage(`Выбрана фигура на ${x},${y}`);
        } else {
            setMessage('Нельзя выбрать эту клетку или не ваш ход');
        }
    };

    const pieceSymbols: { [key: string]: string } = {
        'WHITE_KING': '♔', 'WHITE_QUEEN': '♕', 'WHITE_ROOK': '♖', 'WHITE_BISHOP': '♗',
        'WHITE_KNIGHT': '♘', 'WHITE_PAWN': '♙', 'BLACK_KING': '♚', 'BLACK_QUEEN': '♛',
        'BLACK_ROOK': '♜', 'BLACK_BISHOP': '♝', 'BLACK_KNIGHT': '♞', 'BLACK_PAWN': '♟',
        null: ''
    };

    const getPieceKey = (piece: { color: 'WHITE' | 'BLACK'; type: string } | null): string => {
        return piece ? `${piece.color}_${piece.type}` : 'null';
    };

    return (
        <div className="game-container">
            <div className="game-card">
                <div className="game-header">
                    <FaChessBoard className="game-icon" />
                    <h2>Игра {gameId}</h2>
                </div>
                <div className="game-content">
                    <div className="game-info">
                        <p>Игрок: {playerId}</p>
                        <p>Цвет: {color}</p>
                        <p>Статус: {gameState.status}</p>
                        <p>Ход: {gameState.currentTurnColor === 'WHITE' ? 'Белые' : 'Чёрные'}</p>
                        <p>{message}</p>
                    </div>
                    <div className="game-board">
                        {gameState.board.map((row, x) =>
                            row.map((piece, y) => {
                                const isSelected = gameState.selectedSquare && gameState.selectedSquare[0] === x && gameState.selectedSquare[1] === y;
                                return (
                                    <div
                                        key={`${x}-${y}`}
                                        className={`cell ${((x + y) % 2 === 0) ? 'white' : 'black'} ${isSelected ? 'selected' : ''}`}
                                        onClick={() => handleSquareClick(x, y)}
                                    >
                                        {piece ? pieceSymbols[getPieceKey(piece)] : ''}
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Game;