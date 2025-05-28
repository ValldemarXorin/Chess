import React from 'react';
import { Position, Piece, PieceColor, PieceType } from '../types';
import { useGame } from '../context/GameContext';

interface ChessBoardProps {
    onSquareClick: (position: Position) => void;
}

const ChessBoard: React.FC<ChessBoardProps> = ({ onSquareClick }) => {
    const { board, selectedSquare, possibleMoves, myColor } = useGame();

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
        if (!piece) return null;

        return (
            <div className="chess-piece animate-fade-in-up">
                {pieceSymbols[piece.color][piece.type]}
            </div>
        );
    };

    const isSquareHighlighted = (x: number, y: number): boolean => {
        return selectedSquare?.x === x && selectedSquare?.y === y;
    };

    const isSquarePossibleMove = (x: number, y: number): boolean => {
        return possibleMoves.some(move => move.x === x && move.y === y);
    };

    const getSquareClasses = (x: number, y: number): string => {
        let classes = 'chess-square ';

        // Light or dark square
        classes += (x + y) % 2 === 0 ? 'light ' : 'dark ';

        // Highlighted square
        if (isSquareHighlighted(x, y)) {
            classes += 'highlighted ';
        }

        // Possible move
        if (isSquarePossibleMove(x, y)) {
            classes += 'possible-move ';
        }

        return classes;
    };

    // Flip board if playing as black
    const shouldFlipBoard = myColor === PieceColor.BLACK;

    const renderBoard = () => {
        const rows = [];

        for (let y = 0; y < 8; y++) {
            const cols = [];

            for (let x = 0; x < 8; x++) {
                const displayY = shouldFlipBoard ? 7 - y : y;
                const displayX = shouldFlipBoard ? 7 - x : x;
                const piece = board[displayY]?.[displayX];

                cols.push(
                    <div
                        key={`${displayX}-${displayY}`}
                        className={getSquareClasses(displayX, displayY)}
                        onClick={() => onSquareClick({ x: displayX, y: displayY })}
                    >
                        {renderPiece(piece)}

                        {/* Coordinate labels */}
                        {displayY === (shouldFlipBoard ? 0 : 7) && (
                            <div className="absolute bottom-1 right-1 text-xs opacity-60">
                                {String.fromCharCode(97 + displayX)}
                            </div>
                        )}
                        {displayX === (shouldFlipBoard ? 7 : 0) && (
                            <div className="absolute top-1 left-1 text-xs opacity-60">
                                {shouldFlipBoard ? displayY + 1 : 8 - displayY}
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
};

export default ChessBoard;