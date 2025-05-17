package com.example.chess.engine;

import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.King;
import com.example.chess.engine.pieces.Piece;
import java.util.List;

public final class GameAnalyzer {

    private GameAnalyzer() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static boolean isCheck(Color color, Board board) {
        King king = board.getKing(color);
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

        King king = board.getKing(color);
        Allocation kingAllocation = new Allocation(king);
        return !kingAllocation.hasAnyMoves(board);
    }

    public static boolean isPiecePinned(Piece piece, Board board) {
        Board copyBoard = new Board(board);
        copyBoard.setPieceAt(piece.getCoordX(), piece.getCoordY(), null);
        return GameAnalyzer.isCheck(piece.getColor(), copyBoard);
    }

    public static boolean isStalemate(Color color, Board board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.getPieceAt(i, j).getColor() == color) {
                    Allocation allocation = new Allocation(board.getPieceAt(i, j));
                    if (allocation.hasAnyMoves(board)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
