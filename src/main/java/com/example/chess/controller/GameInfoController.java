package com.example.chess.controller;

import com.example.chess.dto.request.GameInfoDtoRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.service.GameInfoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameInfoController {
    private final GameInfoService gameInfoService;

    public GameInfoController(GameInfoService gameInfoService) {
        this.gameInfoService = gameInfoService;
    }

    @PostMapping
    public ResponseEntity<GameInfoDtoResponse> createGame(@RequestBody
                                                              GameInfoDtoRequest gameInfoDto) {
        GameInfoDtoResponse createdGame = gameInfoService.createGame(gameInfoDto);
        return new ResponseEntity<>(createdGame, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameInfoDtoResponse> getGameById(@PathVariable Long id) {
        try {
            GameInfoDtoResponse gameInfoDto = gameInfoService.getGameById(id);
            return ResponseEntity.ok(gameInfoDto);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<GameInfoDtoResponse>> getAllGames() {
        List<GameInfoDtoResponse> games = gameInfoService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameInfoDtoResponse> updateGame(
            @PathVariable Long id,
            @RequestBody GameInfoDtoRequest gameInfoDto) {
        try {
            GameInfoDtoResponse updatedGame = gameInfoService.updateGame(id, gameInfoDto);
            return ResponseEntity.ok(updatedGame);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        try {
            gameInfoService.deleteGame(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
