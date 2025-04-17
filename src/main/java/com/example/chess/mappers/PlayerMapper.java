package com.example.chess.mappers;

import com.example.chess.dto.request.PlayerRequest;
import com.example.chess.dto.response.PlayerResponse;
import com.example.chess.entity.Player;


public class PlayerMapper {

    private PlayerMapper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static PlayerResponse toDto(Player player) {
        return new PlayerResponse(player.getId(), player.getEmail(), player.getName());
    }

    public static Player toEntity(PlayerRequest playerRequest) {
        Player player = new Player();
        player.setName(playerRequest.getName());
        player.setEmail(playerRequest.getEmail());
        player.setHashPassword(playerRequest.getPassword());
        return player;
    }
}
