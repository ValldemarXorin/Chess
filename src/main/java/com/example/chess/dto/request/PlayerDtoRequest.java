package com.example.chess.dto.request;

import lombok.Data;

@Data
public class PlayerDtoRequest {
    private String name;
    private String email;
    private String password;

    public PlayerDtoRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
