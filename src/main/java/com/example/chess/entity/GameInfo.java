package com.example.chess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "games_info")
public class GameInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "whitePlayer_id")
    private Player whitePlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blackPlayer_id")
    private Player blackPlayer;

    private String status;

    private String notes;

    public GameInfo(LocalDateTime startTime, LocalDateTime endTime,
                    Player whitePlayer, Player blackPlayer,
                    String status, String notes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.status = status;
        this.notes = notes;
    }
}
