package com.example.chess.engine;

import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.exception.game.IllegalMove;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Move {

    public void movePiece(Piece piece, Board board, int endX, int endY) throws IllegalMove {
        if (!board.isWhiteToMove() && piece.getColor() != Color.BLACK
                || board.isWhiteToMove() && piece.getColor() != Color.WHITE) {
            throw new IllegalMove();
        }

        if (GameAnalyzer.isPiecePinned(piece, board)) {
            throw new IllegalMove();
        }

        if (board.getPieceAt(endX, endY) == null) {
            if (!piece.isLegalMove(endX, endY, board)) {
                throw new IllegalMove();
            }
        } else {
            if (!piece.isLegalCapture(board.getPieceAt(endX, endY), board)) {
                throw new IllegalMove();
            }
        }

        board.setPieceAt(endX, endY, piece);
        board.setPieceAt(piece.getCoordX(), piece.getCoordY(), null);
        piece.setCoordX(endX);
        piece.setCoordY(endY);
        board.changeMove();
    }
}
