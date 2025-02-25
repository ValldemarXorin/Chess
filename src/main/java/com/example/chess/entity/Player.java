package com.example.chess.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Player {
    private long id;
    private String email;
    private String hashPassword;
    private String name;


    public Player(long id, String email, String hashPassword,
                  String name) {
        this.id = id;
        this.email = email;
        this.hashPassword = hashPassword;
        this.name = name;
    }
}
