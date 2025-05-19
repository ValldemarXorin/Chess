package com.example.chess.service.implementation;

import com.example.chess.engine.Allocation;
import com.example.chess.engine.Board;
import com.example.chess.engine.GameAnalyzer;
import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import com.example.chess.exception.game.IllegalMove;
import com.example.chess.repository.GameInfoRepository;
import com.example.chess.service.GameService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class GameServiceImpl implements GameService {
    Board board;
    GameInfo gameInfo;
    Player whitePlayer;
    Player blackPlayer;
    List<String> notes;
    GameInfoRepository gameInfoRepository;
    String status;

    public GameServiceImpl(GameInfoRepository gameInfoRepository) {
        this.gameInfoRepository = gameInfoRepository;
    }

    public long initGame(Player whitePlayer, Player blackPlayer) {
        this.board = new Board();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.gameInfo = new GameInfo();
        this.gameInfo.setWhitePlayer(whitePlayer);
        this.gameInfo.setBlackPlayer(blackPlayer);
        this.gameInfo.setStartTime(LocalDateTime.now());
        this.gameInfo.setEndTime(LocalDateTime.now());

        this.gameInfo = gameInfoRepository.save(gameInfo);

        this.board.setId(this.gameInfo.getId());
        this.status = "In process";
        return gameInfo.getId();
    }

    public boolean isCheck(Color color) {
        return GameAnalyzer.isCheck(color, this.board);
    }

    public boolean isCheckmate(Color color) {
        if (GameAnalyzer.isMate(color, this.board)) {
            this.status = "Checkmate";
            return true;
        }
        return false;
    }

    public boolean isStalemate() {
        Color whoMove = board.isWhiteToMove() ? Color.WHITE : Color.BLACK;
        if (GameAnalyzer.isStalemate(whoMove, this.board)) {
            this.status = "Stalemate";
            return true;
        }
        return false;
    }

    public void makeMove(int startX, int startY, int endX, int endY) throws IllegalMove {
        try {
            board.movePiece(board.getPieceAt(startX, startY), endX, endY);
        } catch (IllegalMove e) {
            throw new IllegalMove();
        }

        notes.add(board.toAnnotation(endX, endY));
        board.changeMove();
    }

    public Piece[][] showBoard() {
        return board.getField();
    }

    public List<Pair<Integer, Integer>> calculateMoves(Board board, Color color) {
        List<Pair<Integer, Integer>> moves = new ArrayList<>();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board.getPieceAt(i, j).getColor() == color) {
                    Allocation allocation = new Allocation(board.getPieceAt(i, j));
                    moves.addAll(allocation.calculateAllMoves(board));
                }
            }
        }
        return moves;
    }

    public void endOfGame() {
        this.gameInfo.setEndTime(LocalDateTime.now());
        this.gameInfo.setStatus(this.status);
        this.gameInfo.setNotes(this.notes.toString());
        gameInfoRepository.save(gameInfo);
    }

    public Board getBoard() {
        return board;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }
}
