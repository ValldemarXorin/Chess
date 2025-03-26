package com.example.chess.service;

import com.example.chess.dto.request.GameInfoDtoRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.exception.NotFoundException;
import java.util.List;

public interface GameInfoService {
    GameInfoDtoResponse createGame(GameInfoDtoRequest gameInfoDto);

    GameInfoDtoResponse getGameById(Long id) throws NotFoundException;

    List<GameInfoDtoResponse> getAllGames();

    GameInfoDtoResponse updateGame(Long id, GameInfoDtoRequest gameInfoDto)
            throws NotFoundException;

    void deleteGame(Long id) throws NotFoundException;
}
