package com.example.chess.service;

import com.example.chess.dto.PlayerDto;
import com.example.chess.entity.Player;
import com.example.chess.exception.NotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final Optional<Player>[] players;

    public PlayerService() {
        players = new Optional[3];
        players[0] = Optional.of(new Player(1, "vova3089927@gmail.com", "12345678", "Valldemar"));
        players[1] = Optional.of(new Player(2, "pAlAdin11@email.com", "87654321", "MateMaster"));
        players[2] = Optional.of(new Player(3, "octopus34error@email.com", "18273645", "Octopus"));
    }

    public PlayerDto getPlayerByEmail(String email) throws NotFoundException {
        for (Optional<Player> optionalPlayer : players) {
            if (optionalPlayer.isPresent() && optionalPlayer.get().getEmail().equals(email)) {
                return new PlayerDto(optionalPlayer.get());
            }
        }
        throw new NotFoundException();
    }

    public Optional<PlayerDto> getPlayerByName(String name) throws NotFoundException {
        for (Optional<Player> optionalPlayer : players) {
            if (optionalPlayer.isPresent() && optionalPlayer.get().getName().equals(name)) {
                return Optional.of(new PlayerDto(optionalPlayer.get()));
            }
        }
        throw new NotFoundException();
    }

    public PlayerDto getPlayerByNameAndEmail(String name, String email) throws NotFoundException {
        for (Optional<Player> optionalPlayer : players) {
            if (optionalPlayer.isPresent()
                    && optionalPlayer.get().getName().equals(name)
                    && optionalPlayer.get().getEmail().equals(email)) {
                return new PlayerDto(optionalPlayer.get());
            }
        }
        throw new NotFoundException();
    }
}