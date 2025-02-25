package com.example.chess.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "playerStatistic")
public class PlayerStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Player player;

    @Column(name = "wins")
    private int wins;

    @Column(name = "looses")
    private int looses;

    @Column(name = "draws")
    private int draws;

    @Column(name = "matches")
    private int matches;

    @Column(name = "rating")
    private int rating;

    public PlayerStatistic() {
        this.wins = 0;
        this.looses = 0;
        this.draws = 0;
        this.matches = 0;
        this.rating = 600;
    }
}
