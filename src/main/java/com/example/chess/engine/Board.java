package com.example.chess.engine;

import com.example.chess.engine.pieces.Piece;

public class Board {
    private Piece[][] field;
    private boolean isWhiteToMove;

    public Board() {
        field = new Piece[8][8];
        isWhiteToMove = true;
    }

    public Piece getPieceAt(int x, int y) {
        return field[x][y];
    }
}
