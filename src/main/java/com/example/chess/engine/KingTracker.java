package com.example.chess.engine;

import com.example.chess.engine.pieces.King;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KingTracker {
    private King whiteKing;
    private King blackKing;

    KingTracker(King whiteKing, King blackKing) {
        this.whiteKing = whiteKing;
        this.blackKing = blackKing;
    }
}
