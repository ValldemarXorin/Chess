package com.example.chess.service;

import com.example.chess.entity.Player;

public interface GameManagerService {

    public Long createGame(Player whitePlayer, Player blackPlayer);

    public GameService getActiveGame(long gameId);

}
