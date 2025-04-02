package com.example.chess.engine;

import com.example.chess.engine.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Allocation {
    private Piece chosenPiece;
    private int coordX;
    private int coordY;

    public Allocation(Piece chosenPiece) {
        this.chosenPiece = chosenPiece;
        this.coordX = chosenPiece.getCoordX();
        this.coordY = chosenPiece.getCoordY();
    }

    public List<Move> calculateAllMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((chosenPiece.isLegalMove(i, j, board) || chosenPiece.isLegalMove(j, i, board))
                    && !GameAnalyzer.isCheck(chosenPiece.getColor(), board)) {
                    // дописать добавление в лист ходов
                }
            }
        }
        return moves;
    }

    public boolean hasAnyMoves(Board board) {
        Allocation allocation = new Allocation(chosenPiece);
        return !allocation.calculateAllMoves(board).isEmpty();
    }
}
