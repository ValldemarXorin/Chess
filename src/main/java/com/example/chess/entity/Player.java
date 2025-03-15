package com.example.chess.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String hashPassword;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "whitePlayer", cascade = CascadeType.ALL)
    private List<GameInfo> whiteGameInfos = new ArrayList<>();

    @OneToMany(mappedBy = "blackPlayer", cascade = CascadeType.ALL)
    private List<GameInfo> blackGameInfos = new ArrayList<>();


    public Player(long id, String email, String hashPassword,
                  String name) {
        this.id = id;
        this.email = email;
        this.hashPassword = hashPassword;
        this.name = name;
    }
}
