package com.example.chess.service;

import com.example.chess.dto.PlayerDto;
import com.example.chess.entity.Player;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.utils.PasswordUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public PlayerDto getPlayerByEmail(String email) throws NotFoundException {
        Optional<Player> player = playerRepository.findByEmail(email);
        if (player.isPresent()) {
            return new PlayerDto(player.get());
        }
        throw new NotFoundException();
    }

    public PlayerDto getPlayerById(long id) throws NotFoundException {
        Optional<Player> player = playerRepository.findById(id);
        if (player.isPresent()) {
            return new PlayerDto(player.get());
        }
        throw new NotFoundException();
    }

    public List<PlayerDto> getPlayersByNameAndEmail(String name, String email)
            throws NotFoundException {
        List<Player> players;

        if (name != null && !name.isEmpty()) {
            players = playerRepository.findByName(name);
        } else {
            players = new ArrayList<>();
        }

        if (email != null && !email.isEmpty()) {
            Optional<Player> playerByEmail = playerRepository.findByEmail(email);

            if (!players.isEmpty()) {
                players.clear();
                playerByEmail.ifPresent(players::add);
            } else {
                playerByEmail.ifPresent(players::add);
            }
        }

        if (players.isEmpty()) {
            throw new NotFoundException();
        }

        return players.stream()
                .map(PlayerDto::new)
                .toList();
    }

    public PlayerDto createPlayer(Player player) throws InvalidParamException {
        if (playerRepository.findByEmail(player.getEmail()).isPresent()) {
            throw new InvalidParamException();
        }
        player.setHashPassword(PasswordUtil.hashPassword(player.getHashPassword()));
        playerRepository.save(player);
        return new PlayerDto(player);
    }
}