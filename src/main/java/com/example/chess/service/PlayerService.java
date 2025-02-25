package com.example.chess.service;

import com.example.chess.dto.PlayerDto;
import com.example.chess.entity.Player;
import com.example.chess.entity.PlayerStatistic;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    @Autowired
    PlayerRepository playerRepository;

    public PlayerDto registerPlayer(Player player) throws InvalidParamException {
        if (!playerRepository.existsByEmail(player.getEmail())) {
            throw new InvalidParamException();
        }
        player.setHashPassword(PasswordUtil.encode(player.getHashPassword()));
        playerRepository.save(player);
        return new PlayerDto(player);
    }

    public PlayerDto getPlayerByEmail(String email) throws NotFoundException {
        Optional<Player> playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent()) {
            return new PlayerDto(playerOpt.get());
        }
        throw new NotFoundException();
    }

    public Optional<PlayerDto> getPlayerByName(String name) throws NotFoundException {
        Optional<Player> playerOpt = playerRepository.findByName(name);
        if (playerOpt.isPresent()) {
            return playerOpt.map(PlayerDto::new);
        }
        throw new NotFoundException();
    }

    public List<PlayerDto> getAllPlayers() {
        return playerRepository.findAll().stream().map(PlayerDto::new).collect(Collectors.toList());
    }

    public PlayerDto getPlayerByNameAndEmail(String name, String email) throws NotFoundException {
        Optional<Player> playerOpt = playerRepository.findByEmailAndName(name, email);
        if (playerOpt.isPresent()) {
            return new PlayerDto(playerOpt.get());
        }
        throw new NotFoundException();
    }

    public PlayerDto removePlayerByEmail(String email) throws NotFoundException {
        Optional<Player> playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent()) {
            return new PlayerDto(playerRepository.removeByEmail(email));
        }
        throw new NotFoundException();
    }

    public boolean checkPlayerPassword(String email, String password) {
        Optional<Player> playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent() && PasswordUtil.matches(password, playerOpt.get().getHashPassword())) {
            return true;
        }
        return false;
    }

    public PlayerStatistic getPlayerStatisticByEmail(String email) throws NotFoundException {
        Optional<Player> playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent()) {
            return playerOpt.get().getPlayerStatistic();
        }
        throw new NotFoundException();
    }
}
