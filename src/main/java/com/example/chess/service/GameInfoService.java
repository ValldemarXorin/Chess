package com.example.chess.service;

import com.example.chess.dto.request.GameInfoRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.exception.ResourceNotFoundException;
import java.util.List;

public interface GameInfoService {
    GameInfoResponse createGame(GameInfoRequest gameInfoDto);

    GameInfoResponse getGameById(Long id);

    List<GameInfoResponse> getAllGames();

    GameInfoResponse updateGame(Long id, GameInfoRequest gameInfoDto);

    void deleteGame(Long id)
            throws ResourceNotFoundException;

    GameInfo getCachedGameInfo(Long id)
            throws ResourceNotFoundException;
}
