package com.example.chess.mappers;

import com.example.chess.dto.request.GameInfoDtoRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.entity.GameInfo;

public class GameInfoMapper {
    public static GameInfoDtoResponse toDto(GameInfo gameInfo) {
        return new GameInfoDtoResponse(gameInfo.getId(), gameInfo.getStartTime(), gameInfo.getEndTime(),
                gameInfo.getStatus(), gameInfo.getNotes());
    }

    public static GameInfo toEntity(GameInfoDtoRequest gameInfoDtoRequest) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setStartTime(gameInfoDtoRequest.getStartTime());
        gameInfo.setEndTime(gameInfoDtoRequest.getEndTime());
        gameInfo.setStatus(gameInfoDtoRequest.getStatus());
        gameInfo.setNotes(gameInfoDtoRequest.getNotes());
        return gameInfo;
    }
}
