package com.example.chess.engine;

import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.King;
import com.example.chess.engine.pieces.Piece;

import java.util.List;

public final class GameAnalyzer {

    public static boolean isCheck(Color color, Board board) {
        King king = findKing(color, board);
        if (king == null) {
            return false;
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.getPieceAt(i, j);
                if (piece != null && piece.isLegalCapture(king, board)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMate(Color color, Board board) {
        if (!isCheck(color, board)) {
            return false;
        }

        King king = findKing(color, board);
        Allocation kingAllocation = new Allocation(king);
        return !kingAllocation.hasAnyMoves(board);
    }

    public static King findKing(Color color, Board board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.getPieceAt(i, j);
                if (piece instanceof King && piece.getColor() == color) {
                    return (King) piece;
                }
            }
        }
        return null;
    }

    public static boolean isPiecePinned(Piece piece, Board board) {
        King king = findKing(piece.getColor(), board);

        if (king == null) {
            return false;
        }

        Allocation pieceAllocation = new Allocation(piece);
        List<Move> pieceMoves = pieceAllocation.calculateAllMoves(board);
        for (Move move : pieceMoves) {
            Board clonedBoard = board.clone();
            clonedBoard.setPieceAt(piece.getCoordX(), piece.getCoordY(), null);
            clonedBoard.setPieceAt(move.getEndX(), move.getEndY(), piece);
            if (isCheck(piece.getColor(), clonedBoard)) {
                return true;
            }
        }
        return false;
    }
}
