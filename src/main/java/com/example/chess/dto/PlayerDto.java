package com.example.chess.dto;

import com.example.chess.entity.Player;
import lombok.Data;

@Data
public class PlayerDto {
    private long id;
    private String email;
    private String name;

    public PlayerDto(Player entity) {
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.name = entity.getName();
    }
}
