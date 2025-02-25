package com.example.chess.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "email", unique = true)
    String email;

    @Column(name = "hashPassword")
    String hashPassword;

    @Column(name = "name")
    String name;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    PlayerStatistic playerStatistic;


    public Player(long id, String email, String hashPassword,
                  String name, PlayerStatistic playerStatistic) {
        this.id = id;
        this.email = email;
        this.hashPassword = hashPassword;
        this.name = name;
        this.playerStatistic = playerStatistic;
    }
}
