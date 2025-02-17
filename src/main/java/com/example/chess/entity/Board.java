package com.example.chess.entity;

@Entity
public class Board {
    long id;
    Player whitePlayer;
    Player blackPlayer;

    public Board(long id, Player whitePlayer, Player blackPlayer) {
        this.id = id;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public long getId() {
        return id;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }
}
