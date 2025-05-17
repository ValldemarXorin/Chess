package com.example.chess.engine.pieces;

public enum Color {
    WHITE,
    BLACK;

    @Override
    public String toString() {
        return this == WHITE ? "white" : "black";
    }
}