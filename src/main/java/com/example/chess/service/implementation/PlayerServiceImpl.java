package com.example.chess.service.implementation;

import com.example.chess.dto.response.PlayerDtoResponse;
import com.example.chess.entity.Player;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import com.example.chess.mappers.PlayerMapper;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.PlayerService;
import com.example.chess.utils.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public PlayerDtoResponse getPlayerById(long id) throws NotFoundException {
        Optional<Player> player = playerRepository.findById(id);
        return player.map(PlayerMapper::toDto).orElseThrow(NotFoundException::new);
    }

    @Override
    public List<PlayerDtoResponse> getPlayersByNameAndEmail(String name, String email) throws NotFoundException {
        List<Player> players = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            players.addAll(playerRepository.findByName(name));
        }

        if (email != null && !email.isEmpty()) {
            Optional<Player> playerByEmail = playerRepository.findByEmail(email);
            playerByEmail.ifPresent(p -> {
                players.clear();
                players.add(p);
            });
        }

        if (players.isEmpty()) {
            throw new NotFoundException();
        }

        return players.stream().map(PlayerMapper::toDto).toList();
    }

    @Override
    public PlayerDtoResponse createPlayer(Player player) throws InvalidParamException {
        if (playerRepository.findByEmail(player.getEmail()).isPresent()) {
            throw new InvalidParamException();
        }
        player.setHashPassword(PasswordUtil.hashPassword(player.getHashPassword()));
        playerRepository.save(player);
        return PlayerMapper.toDto(player);
    }
}
