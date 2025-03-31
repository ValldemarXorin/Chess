package com.example.chess.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @OneToMany(mappedBy = "whitePlayer", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                                                    CascadeType.REMOVE},
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<GameInfo> gamesAsWhitePlayer = new ArrayList<>();

    @OneToMany(mappedBy = "blackPlayer", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                                                    CascadeType.REMOVE},
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<GameInfo> gamesAsBlackPlayer = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<Player> friends = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "friend_requests",
            joinColumns = @JoinColumn(name = "sender_user_id"),
            inverseJoinColumns = @JoinColumn(name = "recipient_user_id")
    )
    private Set<Player> friendRequests = new HashSet<>();


    public Player(String email, String hashPassword, String name) {
        this.email = email;
        this.hashPassword = hashPassword;
        this.name = name;
    }
}
