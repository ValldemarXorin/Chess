package com.example.chess.mappers;

import com.example.chess.dto.request.GameInfoRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.repository.PlayerRepository;

public class GameInfoMapper {
    private final PlayerRepository playerRepository;

    private GameInfoMapper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public GameInfoMapper(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public static GameInfoResponse toDto(GameInfo gameInfo) {
        return new GameInfoResponse(gameInfo.getId(), gameInfo.getStartTime(),
                gameInfo.getEndTime(), gameInfo.getStatus(), gameInfo.getNotes(),
                PlayerMapper.toDto(gameInfo.getWhitePlayer()),
                PlayerMapper.toDto(gameInfo.getBlackPlayer()));
    }

    public static GameInfo toEntity(GameInfoRequest gameInfoRequest,
                                    PlayerRepository playerRepository) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setStartTime(gameInfoRequest.getStartTime());
        gameInfo.setEndTime(gameInfoRequest.getEndTime());
        gameInfo.setStatus(gameInfoRequest.getStatus());
        gameInfo.setNotes(gameInfoRequest.getNotes());
        gameInfo.setWhitePlayer(playerRepository.findById(gameInfoRequest
                .getWhitePlayerId()).orElseThrow(()
                    -> new ResourceNotFoundException("White player not found")));
        gameInfo.setBlackPlayer(playerRepository.findById(gameInfoRequest
                .getBlackPlayerId()).orElseThrow(()
                    -> new ResourceNotFoundException("Black player not found")));
        return gameInfo;
    }
}
