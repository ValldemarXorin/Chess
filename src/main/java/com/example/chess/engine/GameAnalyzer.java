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
        King king = board.getKing(piece.getColor());

        return king == null;
    }
}
