package com.example.chess.mappers;

import com.example.chess.dto.request.GameInfoDtoRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.repository.PlayerRepository;

public class GameInfoMapper {
    private final PlayerRepository playerRepository;

    private GameInfoMapper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public GameInfoMapper(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public static GameInfoDtoResponse toDto(GameInfo gameInfo) {
        return new GameInfoDtoResponse(gameInfo.getId(), gameInfo.getStartTime(),
                gameInfo.getEndTime(), gameInfo.getStatus(), gameInfo.getNotes(),
                PlayerMapper.toDto(gameInfo.getWhitePlayer()),
                PlayerMapper.toDto(gameInfo.getBlackPlayer()));
    }

    public static GameInfo toEntity(GameInfoDtoRequest gameInfoDtoRequest,
                                    PlayerRepository playerRepository) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setStartTime(gameInfoDtoRequest.getStartTime());
        gameInfo.setEndTime(gameInfoDtoRequest.getEndTime());
        gameInfo.setStatus(gameInfoDtoRequest.getStatus());
        gameInfo.setNotes(gameInfoDtoRequest.getNotes());
        gameInfo.setWhitePlayer(playerRepository.findById(gameInfoDtoRequest
                .getWhitePlayerId()).get());
        gameInfo.setBlackPlayer(playerRepository.findById(gameInfoDtoRequest
                .getBlackPlayerId()).get());
        return gameInfo;
    }
}
