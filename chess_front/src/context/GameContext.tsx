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
    lastMove: null,
};

// Game reducer
const gameReducer = (state: GameState, action: GameAction): GameState => {
    switch (action.type) {
        case 'START_GAME':
            console.log('START_GAME:', action.payload);
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
            console.log('UPDATE_BOARD:', {
                payload: action.payload,
                isMyTurn: state.myColor === action.payload.currentTurnColor,
                gameStatus: action.payload.status
            });
            if (!action.payload.board || !Array.isArray(action.payload.board) || action.payload.board.length !== 8) {
                console.warn('Некорректное состояние доски в UPDATE_BOARD, игнорируем:', action.payload.board);
                return {
                    ...state,
                    gameStatus: action.payload.status,
                    currentTurn: action.payload.currentTurnColor as PieceColor,
                    isMyTurn: state.myColor === action.payload.currentTurnColor,
                    selectedSquare: null,
                    possibleMoves: [],
                    lastMove: action.payload.lastMove || null,
                };
            }
            // Проверка, не является ли board начальной позицией
            const isInitialBoard = JSON.stringify(action.payload.board) === JSON.stringify(createInitialBoard());
            if (isInitialBoard && state.gameHistory.length > 0) {
                console.warn('Получена начальная доска после ходов, игнорируем:', action.payload.board);
                return {
                    ...state,
                    gameStatus: action.payload.status,
                    currentTurn: action.payload.currentTurnColor as PieceColor,
                    isMyTurn: state.myColor === action.payload.currentTurnColor,
                    selectedSquare: null,
                    possibleMoves: [],
                    lastMove: action.payload.lastMove || null,
                };
            }
            return {
                ...state,
                board: action.payload.board,
                gameStatus: action.payload.status,
                currentTurn: action.payload.currentTurnColor as PieceColor,
                isMyTurn: state.myColor === action.payload.currentTurnColor,
                selectedSquare: null,
                possibleMoves: [],
                lastMove: action.payload.lastMove || null,
            };
        case 'SELECT_SQUARE':
            console.log('SELECT_SQUARE:', action.payload, 'isMyTurn:', state.isMyTurn);
            return {
                ...state,
                selectedSquare: action.payload,
            };
        case 'SET_POSSIBLE_MOVES':
            console.log('SET_POSSIBLE_MOVES:', action.payload);
            return {
                ...state,
                possibleMoves: action.payload,
            };
        case 'ADD_MOVE_TO_HISTORY':
            console.log('ADD_MOVE_TO_HISTORY:', action.payload);
            return {
                ...state,
                gameHistory: [...state.gameHistory, action.payload],
            };
        case 'END_GAME':
            console.log('END_GAME:', action.payload);
            return {
                ...state,
                gameStatus: action.payload.status,
                isMyTurn: false,
                selectedSquare: null,
                possibleMoves: [],
            };
        case 'RESET_GAME':
            console.log('RESET_GAME');
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
    makeMove: (from: Position, to: Position, promotionPiece?: PieceType, isOpponentMove?: boolean) => void;
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

    // Subscribe to game moves
    useEffect(() => {
        if (state.currentGame?.id && user?.id) {
            console.log(`Подписка на обновления игры с ID: ${state.currentGame.id}`);
            webSocketService.subscribeToGameMoves(state.currentGame.id, (move: ChessMoveRequest) => {
                console.log('Получен ход:', move, 'Текущий пользователь:', user.id);
                if (move.playerId !== user.id) {
                    console.log('Обработка хода оппонента:', move);
                    makeMove(
                        { x: move.startX, y: move.startY },
                        { x: move.endX, y: move.endY },
                        move.promotionPiece,
                        true
                    );
                }
            });
            return () => {
                console.log(`Отписка от game-${state.currentGame?.id}`);
                webSocketService.unsubscribe(`game-${state.currentGame?.id}`);
            };
        }
    }, [state.currentGame?.id, user?.id]);

    // Start a new game
    const startGame = (gameId: number, myColor: PieceColor): void => {
        dispatch({ type: 'RESET_GAME' });
        const initialBoard = createInitialBoard();
        dispatch({
            type: 'START_GAME',
            payload: { gameId, myColor, initialBoard }
        });
    };

    const isKingUnderAttack = (
        kingPos: Position,
        board: (Piece | null)[][],
        opponentColor: PieceColor
    ): boolean => {
        for (let i = 0; i < 8; i++) {
            for (let j = 0; j < 8; j++) {
                const piece = board[i][j];
                if (piece && piece.color === opponentColor) {
                    const opponentMoves = calculatePossibleMovesForPiece(
                        { x: j, y: i },
                        board,
                        false
                    );
                    if (opponentMoves.some(move => move.x === kingPos.x && move.y === kingPos.y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    };

    const calculatePossibleMovesForPiece = (
        pos: Position,
        board: (Piece | null)[][],
        checkKingSafety: boolean
    ): Position[] => {
        const piece = board[pos.y]?.[pos.x];
        if (!piece) return [];
        const moves: Position[] = [];

        const isKingSafeAfterMove = (from: Position, to: Position): boolean => {
            const tempBoard = board.map(row => [...row]);
            tempBoard[to.y] = [...tempBoard[to.y]];
            tempBoard[from.y] = [...tempBoard[from.y]];
            const movingPiece = tempBoard[from.y][from.x];
            tempBoard[to.y][to.x] = movingPiece;
            tempBoard[from.y][from.x] = null;
            if (movingPiece) {
                movingPiece.position = to;
            }

            let kingPos: Position | null = null;
            for (let i = 0; i < 8; i++) {
                for (let j = 0; j < 8; j++) {
                    const p = tempBoard[i][j];
                    if (p?.type === PieceType.KING && p.color === piece.color) {
                        kingPos = { x: j, y: i };
                        break;
                    }
                }
                if (kingPos) break;
            }

            if (!kingPos) return true;

            return !isKingUnderAttack(kingPos, tempBoard, piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE);
        };

        switch (piece.type) {
            case PieceType.PAWN:
                const direction = piece.color === PieceColor.WHITE ? -1 : 1;
                const startRow = piece.color === PieceColor.WHITE ? 6 : 1;

                if (!board[pos.y + direction]?.[pos.x]) {
                    moves.push({ x: pos.x, y: pos.y + direction });

                    if (pos.y === startRow && !board[pos.y + 2 * direction]?.[pos.x]) {
                        moves.push({ x: pos.x, y: pos.y + 2 * direction });
                    }
                }

                [-1, 1].forEach(dx => {
                    const targetPiece = board[pos.y + direction]?.[pos.x + dx];
                    if (targetPiece && targetPiece.color !== piece.color) {
                        moves.push({ x: pos.x + dx, y: pos.y + direction });
                    }
                });

                if (state.gameHistory.length > 0) {
                    const lastMove = state.gameHistory[state.gameHistory.length - 1];
                    if (
                        lastMove.piece.type === PieceType.PAWN &&
                        Math.abs(lastMove.from.y - lastMove.to.y) === 2 &&
                        lastMove.to.y === pos.y &&
                        Math.abs(lastMove.to.x - pos.x) === 1
                    ) {
                        moves.push({ x: lastMove.to.x, y: pos.y + direction });
                    }
                }
                break;

            case PieceType.ROOK:
                const rookDirections = [[0, 1], [0, -1], [1, 0], [-1, 0]];
                rookDirections.forEach(([dx, dy]) => {
                    for (let i = 1; i < 8; i++) {
                        const newX = pos.x + dx * i;
                        const newY = pos.y + dy * i;

                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) break;

                        const targetPiece = board[newY][newX];
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
                    const newX = pos.x + dx;
                    const newY = pos.y + dy;

                    if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
                        const targetPiece = board[newY][newX];
                        if (!targetPiece || targetPiece.color !== piece.color) {
                            moves.push({ x: newX, y: newY });
                        }
                    }
                });
                break;

            case PieceType.BISHOP:
                const bishopDirections = [[1, 1], [1, -1], [-1, 1], [-1, -1]];
                bishopDirections.forEach(([dx, dy]) => {
                    for (let i = 1; i < 8; i++) {
                        const newX = pos.x + dx * i;
                        const newY = pos.y + dy * i;

                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) break;

                        const targetPiece = board[newY][newX];
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
                const queenDirections = [[0, 1], [0, -1], [1, 0], [-1, 0], [1, 1], [1, -1], [-1, 1], [-1, -1]];
                queenDirections.forEach(([dx, dy]) => {
                    for (let i = 1; i < 8; i++) {
                        const newX = pos.x + dx * i;
                        const newY = pos.y + dy * i;

                        if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) break;

                        const targetPiece = board[newY][newX];
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
                    const newX = pos.x + dx;
                    const newY = pos.y + dy;

                    if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
                        const targetPiece = board[newY][newX];
                        if (!targetPiece || targetPiece.color !== piece.color) {
                            moves.push({ x: newX, y: newY });
                        }
                    }
                });

                if (!piece.hasMoved) {
                    if (
                        !board[pos.y][pos.x + 1] &&
                        !board[pos.y][pos.x + 2] &&
                        board[pos.y][pos.x + 3]?.type === PieceType.ROOK &&
                        board[pos.y][pos.x + 3]?.color === piece.color &&
                        !board[pos.y][pos.x + 3]?.hasMoved &&
                        !isKingUnderAttack(pos, board, piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE) &&
                        !isKingUnderAttack(
                            { x: pos.x + 1, y: pos.y },
                            board,
                            piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE
                        ) &&
                        !isKingUnderAttack(
                            { x: pos.x + 2, y: pos.y },
                            board,
                            piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE
                        )
                    ) {
                        moves.push({ x: pos.x + 2, y: pos.y });
                    }

                    if (
                        !board[pos.y][pos.x - 1] &&
                        !board[pos.y][pos.x - 2] &&
                        !board[pos.y][pos.x - 3] &&
                        board[pos.y][pos.x - 4]?.type === PieceType.ROOK &&
                        board[pos.y][pos.x - 4]?.color === piece.color &&
                        !board[pos.y][pos.x - 4]?.hasMoved &&
                        !isKingUnderAttack(pos, board, piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE) &&
                        !isKingUnderAttack(
                            { x: pos.x - 1, y: pos.y },
                            board,
                            piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE
                        ) &&
                        !isKingUnderAttack(
                            { x: pos.x - 2, y: pos.y },
                            board,
                            piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE
                        )
                    ) {
                        moves.push({ x: pos.x - 2, y: pos.y });
                    }
                }
                break;
        }

        if (checkKingSafety) {
            return moves.filter(move => isKingSafeAfterMove(pos, move));
        }
        return moves;
    };

    // Calculate possible moves for a piece
    const calculatePossibleMoves = (position: Position): Position[] => {
        console.log('calculatePossibleMoves:', { position, isMyTurn: state.isMyTurn, myColor: state.myColor, gameStatus: state.gameStatus });
        const piece = state.board[position.y]?.[position.x];
        if (!piece || piece.color !== state.myColor || !state.isMyTurn || state.gameStatus !== 'in_progress') {
            console.warn('Нельзя выбрать фигуру:', { piece, myColor: state.myColor, isMyTurn: state.isMyTurn, gameStatus: state.gameStatus });
            return [];
        }
        return calculatePossibleMovesForPiece(position, state.board, true);
    };

    const isCheckmateOrStalemate = (board: (Piece | null)[][]): { isCheckmate: boolean; isStalemate: boolean } => {
        const currentPlayerColor = state.currentTurn;
        const opponentColor = currentPlayerColor === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

        let kingPos: Position | null = null;
        for (let i = 0; i < 8; i++) {
            for (let j = 0; j < 8; j++) {
                const piece = board[i][j];
                if (piece?.type === PieceType.KING && piece.color === currentPlayerColor) {
                    kingPos = { x: j, y: i };
                    break;
                }
            }
            if (kingPos) break;
        }

        if (!kingPos) return { isCheckmate: false, isStalemate: false };

        const isCheck = isKingUnderAttack(kingPos, board, opponentColor);

        let hasLegalMoves = false;
        for (let i = 0; i < 8; i++) {
            for (let j = 0; j < 8; j++) {
                const piece = board[i][j];
                if (piece && piece.color === currentPlayerColor) {
                    const moves = calculatePossibleMovesForPiece({ x: j, y: i }, board, true);
                    if (moves.length > 0) {
                        hasLegalMoves = true;
                        break;
                    }
                }
            }
            if (hasLegalMoves) break;
        }

        return {
            isCheckmate: isCheck && !hasLegalMoves,
            isStalemate: !isCheck && !hasLegalMoves,
        };
    };

    // Select a square and calculate possible moves
    const selectSquare = (position: Position | null): void => {
        console.log('selectSquare:', { position, isMyTurn: state.isMyTurn, gameStatus: state.gameStatus });
        dispatch({ type: 'SELECT_SQUARE', payload: position });
        if (position) {
            const possibleMoves = calculatePossibleMoves(position);
            dispatch({ type: 'SET_POSSIBLE_MOVES', payload: possibleMoves });
        } else {
            dispatch({ type: 'SET_POSSIBLE_MOVES', payload: [] });
        }
    };

    // Make a move
    const makeMove = (from: Position, to: Position, promotionPiece?: PieceType, isOpponentMove: boolean = false): void => {
        console.log('makeMove:', { from, to, promotionPiece, isOpponentMove, isMyTurn: state.isMyTurn, myColor: state.myColor, gameStatus: state.gameStatus });
        if (!state.currentGame?.id || !user) {
            console.error('Cannot make move: game not active');
            return;
        }

        if (state.gameStatus !== 'in_progress') {
            console.error('Game is not in progress:', state.gameStatus);
            return;
        }

        const piece = state.board[from.y]?.[from.x];
        if (!piece) {
            console.error('Invalid piece selection at', from);
            return;
        }

        if (!isOpponentMove && (piece.color !== state.myColor || !state.isMyTurn)) {
            console.error('Not your turn or invalid piece color', { pieceColor: piece.color, myColor: state.myColor, isMyTurn: state.isMyTurn });
            return;
        }

        const possibleMoves = isOpponentMove ? calculatePossibleMovesForPiece(from, state.board, false) : calculatePossibleMoves(from);
        const isValidMove = possibleMoves.some(move => move.x === to.x && move.y === to.y);

        if (!isValidMove) {
            console.error('Invalid move:', { from, to, possibleMoves });
            return;
        }

        let specialMove: 'short_castling' | 'long_castling' | 'en_passant' | 'promotion' | undefined;
        let movePromotionPiece: PieceType | undefined = promotionPiece;

        if (piece.type === PieceType.KING && Math.abs(from.x - to.x) === 2) {
            specialMove = from.x > to.x ? 'long_castling' : 'short_castling';
        }

        if (
            piece.type === PieceType.PAWN &&
            Math.abs(from.x - to.x) === 1 &&
            !state.board[to.y]?.[to.x] &&
            state.gameHistory.length > 0
        ) {
            const lastMove = state.gameHistory[state.gameHistory.length - 1];
            if (
                lastMove.piece.type === PieceType.PAWN &&
                Math.abs(lastMove.from.y - lastMove.to.y) === 2 &&
                lastMove.to.y === from.y &&
                lastMove.to.x === to.x
            ) {
                specialMove = 'en_passant';
            }
        }

        if (piece.type === PieceType.PAWN && (to.y === 0 || to.y === 7)) {
            specialMove = 'promotion';
            movePromotionPiece = movePromotionPiece || PieceType.QUEEN;
        }

        if (!isOpponentMove) {
            const moveRequest: ChessMoveRequest = {
                startX: from.x,
                startY: from.y,
                endX: to.x,
                endY: to.y,
                playerId: user.id,
                specialMove,
                promotionPiece: movePromotionPiece,
            };
            console.log('Отправка хода:', moveRequest);
            webSocketService.sendChessMove(state.currentGame.id, moveRequest);
        }

        const newBoard = state.board.map(row => [...row]);
        newBoard[to.y] = [...newBoard[to.y]];
        newBoard[from.y] = [...newBoard[from.y]];
        newBoard[to.y][to.x] = { ...piece, position: to };
        newBoard[from.y][from.x] = null;

        if (specialMove === 'short_castling') {
            const rook = newBoard[from.y][7];
            newBoard[from.y][5] = rook ? { ...rook, position: { x: 5, y: from.y }, hasMoved: true } : null;
            newBoard[from.y][7] = null;
        } else if (specialMove === 'long_castling') {
            const rook = newBoard[from.y][0];
            newBoard[from.y][3] = rook ? { ...rook, position: { x: 3, y: from.y }, hasMoved: true } : null;
            newBoard[from.y][0] = null;
        } else if (specialMove === 'en_passant') {
            newBoard[from.y][to.x] = null;
        } else if (specialMove === 'promotion' && movePromotionPiece) {
            newBoard[to.y][to.x] = { type: movePromotionPiece, color: piece.color, position: to };
        }

        if (piece.type === PieceType.KING || piece.type === PieceType.ROOK) {
            newBoard[to.y][to.x] = { ...newBoard[to.y][to.x]!, hasMoved: true };
        }

        const nextTurn = state.currentTurn === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        const { isCheckmate, isStalemate } = isCheckmateOrStalemate(newBoard);
        let gameStatus = state.gameStatus;
        let winner: PieceColor | undefined;

        if (isCheckmate) {
            gameStatus = 'checkmate';
            winner = piece.color;
        } else if (isStalemate) {
            gameStatus = 'stalemate';
        }

        const capturedPiece = state.board[to.y]?.[to.x];
        const move: ChessMove = {
            from,
            to,
            piece,
            capturedPiece: capturedPiece || undefined,
            isCheck: isKingUnderAttack(
                to,
                newBoard,
                piece.color === PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE
            ),
            isCheckmate,
            isStalemate,
        };

        console.log('Новое состояние доски:', JSON.stringify(newBoard));
        console.log('После хода:', { nextTurn, isMyTurn: state.myColor === nextTurn, gameStatus });

        // Проверка, не является ли newBoard начальной позицией
        const isNewBoardInitial = JSON.stringify(newBoard) === JSON.stringify(createInitialBoard());
        if (isNewBoardInitial && state.gameHistory.length > 0) {
            console.error('newBoard сброшено в начальную позицию:', newBoard);
            return;
        }

        dispatch({ type: 'ADD_MOVE_TO_HISTORY', payload: move });
        dispatch({
            type: 'UPDATE_BOARD',
            payload: {
                board: newBoard,
                status: gameStatus,
                currentTurnColor: nextTurn,
                lastMove: move,
            },
        });

        if (isCheckmate || isStalemate) {
            dispatch({
                type: 'END_GAME',
                payload: { status: gameStatus, winner },
            });
        }

        selectSquare(null);
    };

    // Reset game
    const resetGame = (): void => {
        console.log('Вызов RESET_GAME');
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