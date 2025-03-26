package com.example.chess.mappers;

import com.example.chess.dto.request.PlayerDtoRequest;
import com.example.chess.dto.response.PlayerDtoResponse;
import com.example.chess.entity.Player;

public class PlayerMapper {

    private PlayerMapper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static PlayerDtoResponse toDto(Player player) {
        return new PlayerDtoResponse(player.getId(), player.getEmail(), player.getName());
    }

    public static Player toEntity(PlayerDtoRequest playerDtoRequest) {
        Player player = new Player();
        player.setName(playerDtoRequest.getName());
        player.setEmail(playerDtoRequest.getEmail());
        player.setHashPassword(playerDtoRequest.getPassword());
        return player;
    }
}
