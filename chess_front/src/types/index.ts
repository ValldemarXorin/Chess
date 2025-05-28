// DTOs based on backend specification
export interface ChessMoveRequest {
    startX: number;
    startY: number;
    endX: number;
    endY: number;
    playerId: number;
}

export interface GameInfoRequest {
    startTime: string;
    endTime: string;
    status: string | null;  // Может быть null
    notes: string | null;   // Может быть null
    whitePlayerId: number;
    blackPlayerId: number;
}

export interface PlayerFilterRequest {
    status: string;
    notes: string;
    page: number;
    size: number;
}

export interface PlayerRequest {
    name: string;
    email: string;
    password: string;
}

export interface ExceptionResponse {
    message: string;
    httpStatus: string;
    timestamp: string;
    path: string;
}

export interface GameInfoResponse {
    id: number;
    startTime: string;
    endTime: string;
    status: string;
    notes: string;
    whitePlayer: PlayerResponse;
    blackPlayer: PlayerResponse;
}

export interface GameStateResponse {
    board: Piece[][];
    status: string;
    currentTurnColor: string;
}

export interface MatchFoundResponse {
    gameId: number;
    color: string;
}

export interface PlayerResponse {
    id: number;
    email: string;
    name: string;
}

// Additional types for chess game
export interface Piece {
    type: PieceType;
    color: PieceColor;
    position: Position;
    hasMoved?: boolean;
}

export enum PieceType {
    KING = 'king',
    QUEEN = 'queen',
    ROOK = 'rook',
    BISHOP = 'bishop',
    KNIGHT = 'knight',
    PAWN = 'pawn'
}

export enum PieceColor {
    WHITE = 'white',
    BLACK = 'black'
}

export interface Position {
    x: number;
    y: number;
}

export interface ChessMove {
    from: Position;
    to: Position;
    piece: Piece;
    capturedPiece?: Piece;
    isCheck?: boolean;
    isCheckmate?: boolean;
    isStalemate?: boolean;
}

// Application state types
export interface AuthState {
    user: PlayerResponse | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
}

export interface GameState {
    currentGame: GameInfoResponse | null;
    board: (Piece | null)[][];
    gameStatus: string;
    currentTurn: PieceColor;
    selectedSquare: Position | null;
    possibleMoves: Position[];
    gameHistory: ChessMove[];
    isMyTurn: boolean;
    myColor: PieceColor | null;
}

export interface FriendRequest {
    id: number;
    sender: PlayerResponse;
    receiver: PlayerResponse;
    status: 'pending' | 'accepted' | 'rejected';
    createdAt: string;
}

// API Response types
export interface ApiResponse<T> {
    data: T;
    success: boolean;
    message?: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

// WebSocket message types
export interface WebSocketMessage {
    type: string;
    payload: any;
    timestamp: string;
}

export interface MatchmakingStatus {
    inQueue: boolean;
    estimatedWaitTime?: number;
    queuePosition?: number;
}

export interface OnlineStatus {
    playerId: number;
    status: 'ONLINE' | 'OFFLINE';
}

export interface PlayerUpdateRequest {
    name?: string;
    email?: string;
}

export interface PlayerResponse {
    id: number;
    name: string;
    email: string;
    ratingScore?: number;
}