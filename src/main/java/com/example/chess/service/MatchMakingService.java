package com.example.chess.service;

public interface MatchMakingService {
    public void processMatchmaking();

    public void addPlayerToQueue(Long playerId);

    public void removePlayerFromQueue(Long playerId);
}
