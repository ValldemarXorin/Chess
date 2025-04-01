package com.example.chess.controller;

import com.example.chess.dto.request.PlayerDtoRequest;
import com.example.chess.dto.request.PlayerFilterRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.dto.response.PlayerDtoResponse;
import com.example.chess.entity.Player;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import com.example.chess.service.PlayerService;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDtoResponse> findPlayerById(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(playerService.getPlayerById(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping
    public ResponseEntity<List<PlayerDtoResponse>> findPlayersByNameAndEmail(@RequestParam(required
                                                                                 = false)
                                                                         String name,
                                                                     @RequestParam(required
                                                                             = false)
                                                                     String email) {
        try {
            List<PlayerDtoResponse> playersDto =
                    playerService.getPlayersByNameAndEmail(name, email);
            return ResponseEntity.ok(playersDto);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<PlayerDtoResponse> createPlayer(@RequestBody Player player) {
        try {
            return ResponseEntity.ok(playerService.createPlayer(player));
        } catch (InvalidParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/gamesInfo")
    public ResponseEntity<List<GameInfoDtoResponse>> getGamesInfo(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(playerService.getAllGamesInfo(id));
        } catch (InvalidParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<PlayerDtoResponse>> getFriends(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(playerService.getAllFriends(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/send_friend_request")
    public ResponseEntity<PlayerDtoResponse> sendFriendRequest(@PathVariable Long id,
                                                               @RequestBody String friendEmail) {
        try {
            friendEmail = friendEmail.replace("\"", ""); // Убираем кавычки
            return ResponseEntity.ok(playerService.sendFriendRequest(id, friendEmail));
        } catch (InvalidParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/get_friend_request")
    public ResponseEntity<Set<PlayerDtoResponse>> getFriendRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(playerService.getFriendRequests(id));
        } catch (InvalidParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/remove_friend")
    public ResponseEntity<PlayerDtoResponse> removeFriend(@PathVariable Long id,
                                                          @RequestBody String friendEmail) {
        try {
            friendEmail = friendEmail.replace("\"", "");
            return ResponseEntity.ok(playerService.deleteFriend(id, friendEmail));
        } catch (InvalidParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PlayerDtoResponse> removePlayer(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(playerService.deletePlayerById(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerDtoResponse> updatePlayer(
            @PathVariable Long id,
            @RequestBody PlayerDtoRequest playerDtoRequest) {
        try {
            PlayerDtoResponse updatedPlayer = playerService.updatePlayerById(id, playerDtoRequest);
            return ResponseEntity.ok(updatedPlayer);
        } catch (InvalidParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<PlayerDtoResponse>> filterPlayers(
            @ModelAttribute PlayerFilterRequest filter
    ) {
        return ResponseEntity.ok(playerService.getPlayersByFilters(filter));
    }
}