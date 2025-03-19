package com.example.chess.dto.response;

import lombok.Data;

@Data
public class PlayerDtoResponse {
    private long id;
    private String email;
    private String name;

    public PlayerDtoResponse(long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
}
