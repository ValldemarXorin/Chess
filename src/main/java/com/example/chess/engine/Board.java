package com.example.chess.engine;

import com.example.chess.engine.pieces.Piece;

public class Board {
    private Piece[][] board;
    private boolean isWhiteToMove;

    public Board() {
        board = new Piece[8][8];
        isWhiteToMove = true;
    }

    public Piece getPieceAt(int x, int y) {
        return board[x][y];
    }
}
