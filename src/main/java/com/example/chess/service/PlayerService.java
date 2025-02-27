package com.example.chess.service;

import com.example.chess.dto.PlayerDto;
import com.example.chess.entity.Player;
import com.example.chess.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final Player[] players;

    public PlayerService() {
        players = new Player[4];
        players[0] = new Player(1, "vova3089927@gmail.com", "12345678", "Valldemar");
        players[1] = new Player(2, "pAlAdin11@email.com", "87654321", "MateMaster");
        players[2] = new Player(3, "octopus34error@email.com", "18273645", "Octopus");
        players[3] = new Player(4, "cocococor@email.com", "1827344", "MateMaster");
    }

    public PlayerDto getPlayerByEmail(String email) throws NotFoundException {
        for (Player player : players) {
            if (player.getEmail().equals(email)) {
                return new PlayerDto(player);
            }
        }
        throw new NotFoundException();
    }

    public PlayerDto getPlayerById(long id) throws NotFoundException {
        for (Player player : players) {
            if (player.getId() == id) {
                return new PlayerDto(player);
            }
        }
        throw new NotFoundException();
    }

    public List<PlayerDto> getPlayersByNameAndEmail(String name, String email)
            throws NotFoundException {
        List<PlayerDto> foundPlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.getName().equals(name)) {
                if (email == null || player.getEmail().equals(email)) {
                    foundPlayers.add(new PlayerDto(player));
                }
            }
        }
        if (foundPlayers.isEmpty()) {
            throw new NotFoundException();
        }
        return foundPlayers;
    }
}