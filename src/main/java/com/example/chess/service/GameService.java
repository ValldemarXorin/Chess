package com.example.chess.service;

import com.example.chess.engine.Board;
import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import com.example.chess.exception.game.IllegalMove;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;


public interface GameService {
    public long initGame(Player whitePlayer, Player blackPlayer);

    public boolean isCheck(Color color);

    public boolean isCheckmate(Color color);

    public boolean isStalemate();

    public void makeMove(int startX, int startY, int endX, int endY) throws IllegalMove;

    public Piece[][] showBoard();

    public List<Pair<Integer, Integer>> calculateMoves(Board board, Color color);

    public void endOfGame();

    public Board getBoard();

    public GameInfo getGameInfo();
}
