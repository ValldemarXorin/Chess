package com.example.chess.dto;

import com.example.chess.entity.Player;
import com.example.chess.entity.PlayerStatistic;
import lombok.Data;

@Data
public class PlayerDto {
    private long id;
    private String email;
    private String name;
    private PlayerStatistic playerStatistic;

    public PlayerDto(Player entity) {
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.name = entity.getName();
        this.playerStatistic = entity.getPlayerStatistic();
    }
}
