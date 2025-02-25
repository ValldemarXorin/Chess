package com.example.chess.controller;

import com.example.chess.dto.PlayerDto;
import com.example.chess.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<PlayerDto> findPlayerByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(playerService.getPlayerByEmail(email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<PlayerDto> findPlayerByEmailAndName(@RequestParam String email,
                                                              @RequestParam String name) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(playerService.getPlayerByNameAndEmail(name, email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
