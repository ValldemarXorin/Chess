package com.example.chess.controller;

import com.example.chess.dto.PlayerDto;
import com.example.chess.entity.Player;
import com.example.chess.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Repository
@RequestMapping("/players")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @GetMapping
    public ResponseEntity<Optional<PlayerDto>> findPlayerByName(@RequestParam String name) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(playerService.getPlayerByName(name));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<PlayerDto> findPlayerByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(playerService.getPlayerByEmail(email));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<PlayerDto> findPlayerByEmailAndName(@RequestParam String email, @RequestParam String name) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(playerService.getPlayerByNameAndEmail(email, name));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
