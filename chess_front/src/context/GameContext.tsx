import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { GameState, Piece, Position, ChessMove, PieceColor, PieceType, GameStateResponse, ChessMoveRequest } from '../types';
import { useAuth } from './AuthContext';
import webSocketService from '../services/websocket';

// Game actions
type GameAction =
    | { type: 'START_GAME'; payload: { gameId: number; myColor: PieceColor; initialBoard: Piece[][] } }
    | { type: 'UPDATE_BOARD'; payload: GameStateResponse }
    | { type: 'SELECT_SQUARE'; payload: Position | null }
    | { type: 'SET_POSSIBLE_MOVES'; payload: Position[] }
    | { type: 'ADD_MOVE_TO_HISTORY'; payload: ChessMove }
    | { type: 'END_GAME'; payload: { status: string; winner?: PieceColor } }
    | { type: 'RESET_GAME' };

// Initial chess board setup
const createInitialBoard = (): Piece[][] => {
    const board: Piece[][] = Array(8).fill(null).map(() => Array(8).fill(null));

    // Set up pawns
    for (let i = 0; i < 8; i++) {
        board[1][i] = { type: PieceType.PAWN, color: PieceColor.BLACK, position: { x: i, y: 1 } };
        board[6][i] = { type: PieceType.PAWN, color: PieceColor.WHITE, position: { x: i, y: 6 } };
    }

    // Set up other pieces
    const pieceOrder = [PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK];

    for (let i = 0; i < 8; i++) {
        board[0][i] = { type: pieceOrder[i], color: PieceColor.BLACK, position: { x: i, y: 0 } };
        board[7][i] = { type: pieceOrder[i], color: PieceColor.WHITE, position: { x: i, y: 7 } };
    }

    return board;
};

// Initial state
const initialState: GameState = {
    currentGame: null,
    board: createInitialBoard(),
    gameStatus: 'waiting',
    currentTurn: PieceColor.WHITE,
    selectedSquare: null,
    possibleMoves: [],
    gameHistory: [],
    isMyTurn: false,
    myColor: null,
};

// Game reducer
const gameReducer = (state: GameState, action: GameAction): GameState => {
    switch (action.type) {
        case 'START_GAME':
            return {
                ...state,
                currentGame: { id: action.payload.gameId } as any,
                board: action.payload.initialBoard,
                gameStatus: 'in_progress',
                myColor: action.payload.myColor,
                isMyTurn: action.payload.myColor === PieceColor.WHITE,
                selectedSquare: null,
                possibleMoves: [],
                gameHistory: [],
            };
        case 'UPDATE_BOARD':
            return {
                ...state,
                board: action.payload.board,
                gameStatus: action.payload.status,
                currentTurn: action.payload.currentTurnColor as PieceColor,
                isMyTurn: state.myColor === action.payload.currentTurnColor,
                selectedSquare: null,
                possibleMoves: [],
            };
        case 'SELECT_SQUARE':
            return {
                ...state,
                selectedSquare: action.payload,
            };
        case 'SET_POSSIBLE_MOVES':
            return {
                ...state,
                possibleMoves: action.payload,
            };
        case 'ADD_MOVE_TO_HISTORY':
            return {
                ...state,
                gameHistory: [...state.gameHistory, action.payload],
            };
        case 'END_GAME':
            return {
                ...state,
                gameStatus: action.payload.status,
                isMyTurn: false,
                selectedSquare: null,
                possibleMoves: [],
            };
        case 'RESET_GAME':
            return {
                ...initialState,
                board: createInitialBoard(),
            };
        default:
            return state;
    }
};

// Game context type
interface GameContextType extends GameState {
    startGame: (gameId: number, myColor: PieceColor) => void;
    makeMove: (from: Position, to: Position) => void;
    selectSquare: (position: Position | null) => void;
    calculatePossibleMoves: (position: Position) => Position[];
    resetGame: () => void;
}

// Create context
const GameContext = createContext<GameContextType | undefined>(undefined);

// Game provider component
interface GameProviderProps {
    children: ReactNode;
}

export const GameProvider: React.FC<GameProviderProps> = ({ children }) => {
    const [state, dispatch] = useReducer(gameReducer, initialState);
    const { user } = useAuth();

    // Subscribe to game updates when game starts
    useEffect(() => {
        if (state.currentGame?.id) {
            webSocketService.subscribeToGameMoves(state.currentGame.id, (gameState: GameStateResponse) => {
                console.log('Received game state update:', gameState);
                dispatch({ type: 'UPDATE_BOARD', payload: gameState });
            });

            return () => {
                if (state.currentGame?.id) {  // <-- Опциональная цепочка
                    webSocketService.unsubscribe(`game-${state.currentGame.id}`);
                }
            };
        }
    }, [state.currentGame?.id]);

    // Start a new game
    const startGame = (gameId: number, myColor: PieceColor): void => {
        // Сначала сбрасываем предыдущее состояние
        dispatch({ type: 'RESET_GAME' });

        // Затем инициализируем новую игру
        const initialBoard = createInitialBoard();
        dispatch({
            type: 'START_GAME',
            payload: { gameId, myColor, initialBoard }
        });

        // Подписываемся на обновления игры
        webSocketService.subscribeToGameMoves(gameId, (gameState: GameStateResponse) => {
            dispatch({ type: 'UPDATE_BOARD', payload: gameState });
        });
    };

    // Calculate possible moves for a piece
    const calculatePossibleMoves = (position: Position): Position[] => {
        const piece = state.board[position.y]?.[position.x];
        if (!piece || piece.color !== state.myColor || !state.isMyTurn) {
            return [];
        }

        const moves: Position[] = [];
        const { x, y } = position;

        // Basic move calculation (simplified for demo)
        switch (piece.type) {
            case PieceType.PAWN:
                const direction = piece.color === PieceColor.WHITE ? -1 : 1;
                const startRow = piece.color === PieceColor.WHITE ? 6 : 1;

                // Forward move
                if (!state.board[y + direction]?.[x]) {
                    moves.push({ x, y: y + direction });

                    // Double move from start position
                    if (y === startRow && !state.board[y + 2 * direction]?.[x]) {
                        moves.push({ x, y: y + 2 * direction });
                    }
                }

                // Capture moves
                [-1, 1].forEach(dx => {
                    const targetPiece = state.board[y + direction]?.[x + dx];
                    if (targetPiece && targetPiece.color !== piece.color) {
                        moves.push({ x: x + dx, y: y + direction });
                    }
                });
                break;

            case PieceType.ROOK:
                // Horizontal and vertical moves
                const rookDirections = [[0, 1], [0, -1], [1, 0], [-1, 0]];
                rookDirections.forEach(([dx, dy]) => {
                    for (let i = 1; i < 8; i++) {
                        const newX = x + dx * i;
                        const newY = y + dy * i;

                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) break;

                        const targetPiece = state.board[newY][newX];
                        if (targetPiece) {
                            if (targetPiece.color !== piece.color) {
                                moves.push({ x: newX, y: newY });
                            }
                            break;
                        }
                        moves.push({ x: newX, y: newY });
                    }
                });
                break;

            case PieceType.KNIGHT:
                const knightMoves = [[-2, -1], [-2, 1], [-1, -2], [-1, 2], [1, -2], [1, 2], [2, -1], [2, 1]];
                knightMoves.forEach(([dx, dy]) => {
                    const newX = x + dx;
                    const newY = y + dy;

                    if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
                        const targetPiece = state.board[newY][newX];
                        if (!targetPiece || targetPiece.color !== piece.color) {
                            moves.push({ x: newX, y: newY });
                        }
                    }
                });
                break;

            case PieceType.BISHOP:
                // Diagonal moves
                const bishopDirections = [[1, 1], [1, -1], [-1, 1], [-1, -1]];
                bishopDirections.forEach(([dx, dy]) => {
                    for (let i = 1; i < 8; i++) {
                        const newX = x + dx * i;
                        const newY = y + dy * i;

                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) break;

                        const targetPiece = state.board[newY][newX];
                        if (targetPiece) {
                            if (targetPiece.color !== piece.color) {
                                moves.push({ x: newX, y: newY });
                            }
                            break;
                        }
                        moves.push({ x: newX, y: newY });
                    }
                });
                break;

            case PieceType.QUEEN:
                // Combination of rook and bishop moves
                const queenDirections = [[0, 1], [0, -1], [1, 0], [-1, 0], [1, 1], [1, -1], [-1, 1], [-1, -1]];
                queenDirections.forEach(([dx, dy]) => {
                    for (let i = 1; i < 8; i++) {
                        const newX = x + dx * i;
                        const newY = y + dy * i;

                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) break;

                        const targetPiece = state.board[newY][newX];
                        if (targetPiece) {
                            if (targetPiece.color !== piece.color) {
                                moves.push({ x: newX, y: newY });
                            }
                            break;
                        }
                        moves.push({ x: newX, y: newY });
                    }
                });
                break;

            case PieceType.KING:
                const kingDirections = [[0, 1], [0, -1], [1, 0], [-1, 0], [1, 1], [1, -1], [-1, 1], [-1, -1]];
                kingDirections.forEach(([dx, dy]) => {
                    const newX = x + dx;
                    const newY = y + dy;

                    if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
                        const targetPiece = state.board[newY][newX];
                        if (!targetPiece || targetPiece.color !== piece.color) {
                            moves.push({ x: newX, y: newY });
                        }
                    }
                });
                break;
        }

        return moves.filter(move => move.x >= 0 && move.x < 8 && move.y >= 0 && move.y < 8);
    };

    // Select a square and calculate possible moves
    const selectSquare = (position: Position | null): void => {
        dispatch({ type: 'SELECT_SQUARE', payload: position });

        if (position) {
            const possibleMoves = calculatePossibleMoves(position);
            dispatch({ type: 'SET_POSSIBLE_MOVES', payload: possibleMoves });
        } else {
            dispatch({ type: 'SET_POSSIBLE_MOVES', payload: [] });
        }
    };

    // Make a move
    const makeMove = (from: Position, to: Position): void => {
        if (!state.currentGame?.id || !user || !state.isMyTurn) {
            console.error('Cannot make move: game not active or not your turn');
            return;
        }

        const piece = state.board[from.y]?.[from.x];
        if (!piece || piece.color !== state.myColor) {
            console.error('Invalid piece selection');
            return;
        }

        // Check if move is valid
        const possibleMoves = calculatePossibleMoves(from);
        const isValidMove = possibleMoves.some(move => move.x === to.x && move.y === to.y);

        if (!isValidMove) {
            console.error('Invalid move');
            return;
        }

        // Create move request
        const moveRequest: ChessMoveRequest = {
            startX: from.x,
            startY: from.y,
            endX: to.x,
            endY: to.y,
            playerId: user.id,
        };

        // Send move via WebSocket
        webSocketService.sendChessMove(state.currentGame.id, moveRequest);

        // Add move to history
        const capturedPiece = state.board[to.y]?.[to.x];
        const move: ChessMove = {
            from,
            to,
            piece,
            capturedPiece: capturedPiece || undefined,
        };

        dispatch({ type: 'ADD_MOVE_TO_HISTORY', payload: move });

        // Clear selection
        selectSquare(null);
    };

    // Reset game
    const resetGame = (): void => {
        dispatch({ type: 'RESET_GAME' });
    };

    const value: GameContextType = {
        ...state,
        startGame,
        makeMove,
        selectSquare,
        calculatePossibleMoves,
        resetGame,
    };

    return (
        <GameContext.Provider value={value}>
            {children}
        </GameContext.Provider>
    );
};

// Custom hook to use game context
export const useGame = (): GameContextType => {
    const context = useContext(GameContext);
    if (context === undefined) {
        throw new Error('useGame must be used within a GameProvider');
    }
    return context;
};

export default GameContext;