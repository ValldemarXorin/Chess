import React from 'react';
import { Position, Piece, PieceColor, PieceType } from '../types';
import { useGame } from '../context/GameContext';

interface ChessBoardProps {
    onSquareClick: (position: Position) => void;
}

const ChessBoard: React.FC<ChessBoardProps> = ({ onSquareClick }) => {
    const { board, selectedSquare, possibleMoves, myColor } = useGame();

    // Проверка валидности board
    if (!board || !Array.isArray(board) || board.length !== 8 || !board.every(row => Array.isArray(row) && row.length === 8)) {
        console.error('Некорректное состояние доски:', board);
        return <div className="text-red-500">Ошибка: доска не загружена</div>;
    }

    console.log('Рендеринг доски:', JSON.stringify(board));

    // Chess piece Unicode symbols
    const pieceSymbols: Record<PieceColor, Record<PieceType, string>> = {
        [PieceColor.WHITE]: {
            [PieceType.KING]: '♔',
            [PieceType.QUEEN]: '♕',
            [PieceType.ROOK]: '♖',
            [PieceType.BISHOP]: '♗',
            [PieceType.KNIGHT]: '♘',
            [PieceType.PAWN]: '♙',
        },
        [PieceColor.BLACK]: {
            [PieceType.KING]: '♚',
            [PieceType.QUEEN]: '♛',
            [PieceType.ROOK]: '♜',
            [PieceType.BISHOP]: '♝',
            [PieceType.KNIGHT]: '♞',
            [PieceType.PAWN]: '♟',
        },
    };

    const renderPiece = (piece: Piece | null): React.ReactNode => {
        if (!piece || !piece.color || !piece.type) {
            return null;
        }

        try {
            const symbol = pieceSymbols[piece.color][piece.type];
            return (
                <div className="chess-piece animate-fade-in-up">
                    {symbol}
                </div>
            );
        } catch (error) {
            console.error('Ошибка рендеринга фигуры:', piece, error);
            return null;
        }
    };

    const shouldFlipBoard = myColor === PieceColor.BLACK;

    const isSquareHighlighted = (boardX: number, boardY: number): boolean => {
        return selectedSquare?.x === boardX && selectedSquare?.y === boardY;
    };

    const isSquarePossibleMove = (boardX: number, boardY: number): boolean => {
        return possibleMoves.some(move => move.x === boardX && move.y === boardY);
    };

    const getSquareClasses = (boardX: number, boardY: number, displayX: number, displayY: number): string => {
        const classes = [
            'chess-square',
            (displayX + displayY) % 2 === 0 ? 'light' : 'dark',
            isSquareHighlighted(boardX, boardY) ? 'highlighted' : '',
            isSquarePossibleMove(boardX, boardY) ? 'possible-move' : '',
        ];
        return classes.filter(cls => cls).join(' ');
    };

    const renderBoard = () => {
        const rows = [];

        // Для черных переворачиваем порядок строк и столбцов
        const startY = shouldFlipBoard ? 7 : 0;
        const endY = shouldFlipBoard ? -1 : 8;
        const stepY = shouldFlipBoard ? -1 : 1;
        const startX = shouldFlipBoard ? 7 : 0;
        const endX = shouldFlipBoard ? -1 : 8;
        const stepX = shouldFlipBoard ? -1 : 1;

        for (let y = startY; y !== endY; y += stepY) {
            const cols = [];

            for (let x = startX; x !== endX; x += stepX) {
                // Координаты для отображения (нужны только для нумерации и цвета клеток)
                const displayX = shouldFlipBoard ? 7 - x : x;
                const displayY = shouldFlipBoard ? 7 - y : y;
                // Координаты для данных доски
                const boardX = x;
                const boardY = y;
                const piece = board[boardY]?.[boardX];

                cols.push(
                    <div
                        key={`${boardX}-${boardY}`}
                        className={getSquareClasses(boardX, boardY, displayX, displayY)}
                        onClick={() => {
                            console.log(`Clicked: display(${displayX},${displayY}), board(${boardX},${boardY})`);
                            onSquareClick({ x: boardX, y: boardY });
                        }}
                    >
                        {renderPiece(piece)}

                        {/* Coordinate labels */}
                        {boardY === (shouldFlipBoard ? 0 : 7) && (
                            <div className="absolute bottom-1 right-1 text-xs opacity-60">
                                {String.fromCharCode(97 + (shouldFlipBoard ? 7 - boardX : boardX))}
                            </div>
                        )}
                        {boardX === (shouldFlipBoard ? 0 : 7) && (
                            <div className="absolute top-1 left-1 text-xs opacity-60">
                                {shouldFlipBoard ? 8 - boardY : boardY + 1}
                            </div>
                        )}
                    </div>
                );
            }

            rows.push(
                <div key={y} className="flex">
                    {cols}
                </div>
            );
        }

        return rows;
    };

    try {
        return (
            <div className="relative">
                <div className="inline-block border-4 border-chess-gold rounded-lg overflow-hidden shadow-2xl animate-board-glow">
                    <div className="grid grid-rows-8 w-96 h-96 md:w-[500px] md:h-[500px]">
                        {renderBoard()}
                    </div>
                </div>

                {/* Board coordinates */}
                <div className="absolute -bottom-8 left-0 right-0 flex justify-around text-chess-gold text-sm font-semibold">
                    {Array.from({ length: 8 }, (_, i) => (
                        <span key={i}>
                            {String.fromCharCode(97 + (shouldFlipBoard ? 7 - i : i))}
                        </span>
                    ))}
                </div>

                <div className="absolute -left-8 top-0 bottom-0 flex flex-col justify-around text-chess-gold text-sm font-semibold">
                    {Array.from({ length: 8 }, (_, i) => (
                        <span key={i}>
                            {shouldFlipBoard ? i + 1 : 8 - i}
                        </span>
                    ))}
                </div>
            </div>
        );
    } catch (error) {
        console.error('Ошибка рендеринга ChessBoard:', error);
        return <div className="text-red-500">Ошибка отображения доски</div>;
    }
};

export default ChessBoard;