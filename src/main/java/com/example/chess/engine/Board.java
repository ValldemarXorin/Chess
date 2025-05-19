package com.example.chess.engine;

import com.example.chess.engine.pieces.Bishop;
import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.Horse;
import com.example.chess.engine.pieces.King;
import com.example.chess.engine.pieces.Pawn;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.pieces.Queen;
import com.example.chess.engine.pieces.Rook;
import com.example.chess.entity.Player;
import com.example.chess.exception.game.IllegalMove;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Board {
    private long id;
    private KingTracker kingTracker;
    private Piece[][] field;
    private boolean isWhiteToMove;

    public Board() {
        field = new Piece[8][8];
        isWhiteToMove = true;
        initializeBoard();
        kingTracker = new KingTracker((King) field[4][0], (King) field[4][7]);
    }

    public Board(Board board) {

        this.isWhiteToMove = board.isWhiteToMove;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = this.field[x][y].copy();
                if (piece != null) {
                    this.setPieceAt(x, y, piece.copy());
                } else {
                    this.setPieceAt(x, y, null);
                }
            }
        }
    }

    public Piece getPieceAt(int x, int y) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates are out of bounds");
        }
        return field[x][y];
    }

    public void setPieceAt(int x, int y, Piece piece) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates are out of bounds");
        }
        field[x][y] = piece;
    }

    private void initializeBoard() {
        // Расстановка белых фигур
        field[0][0] = new Rook(Color.WHITE, 0, 0);   // Ладья
        field[1][0] = new Horse(Color.WHITE, 1, 0); // Конь
        field[2][0] = new Bishop(Color.WHITE, 2, 0); // Слон
        field[3][0] = new Queen(Color.WHITE, 3, 0);  // Ферзь
        field[4][0] = new King(Color.WHITE, 4, 0);   // Король
        field[5][0] = new Bishop(Color.WHITE, 5, 0); // Слон
        field[6][0] = new Horse(Color.WHITE, 6, 0); // Конь
        field[7][0] = new Rook(Color.WHITE, 7, 0);   // Ладья

        // Расстановка белых пешек
        for (int i = 0; i < 8; i++) {
            field[i][1] = new Pawn(Color.WHITE, i, 1);
        }

        // Расстановка черных фигур
        field[0][7] = new Rook(Color.BLACK, 0, 7);   // Ладья
        field[1][7] = new Horse(Color.BLACK, 1, 7); // Конь
        field[2][7] = new Bishop(Color.BLACK, 2, 7); // Слон
        field[3][7] = new Queen(Color.BLACK, 3, 7);  // Ферзь
        field[4][7] = new King(Color.BLACK, 4, 7);   // Король
        field[5][7] = new Bishop(Color.BLACK, 5, 7); // Слон
        field[6][7] = new Horse(Color.BLACK, 6, 7); // Конь
        field[7][7] = new Rook(Color.BLACK, 7, 7);   // Ладья

        // Расстановка черных пешек
        for (int i = 0; i < 8; i++) {
            field[i][6] = new Pawn(Color.BLACK, i, 6);
        }
    }

    public void changeMove() {
        isWhiteToMove = !isWhiteToMove;
    }

    public King getKing(Color color) {
        return color == Color.WHITE ? kingTracker.getWhiteKing() : kingTracker.getBlackKing();
    }

    public void movePiece(Piece piece, int endX, int endY) throws IllegalMove {
        if (!this.isWhiteToMove() && piece.getColor() != Color.BLACK
                || this.isWhiteToMove() && piece.getColor() != Color.WHITE) {
            throw new IllegalMove();
        }

        if (GameAnalyzer.isPiecePinned(piece, this)) {
            throw new IllegalMove();
        }

        if (this.getPieceAt(endX, endY) == null) {
            if (!piece.isLegalMove(endX, endY, this)) {
                throw new IllegalMove();
            }
        } else {
            if (!piece.isLegalCapture(this.getPieceAt(endX, endY), this)) {
                throw new IllegalMove();
            }
        }

        this.setPieceAt(endX, endY, piece);
        this.setPieceAt(piece.getCoordX(), piece.getCoordY(), null);
        piece.setCoordX(endX);
        piece.setCoordY(endY);
    }

    public String toAnnotation(int x, int y) {
        return "" + (char) ('A' + x) + (y + 1);
    }
}
