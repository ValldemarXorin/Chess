package com.example.chess.service.implementation;

import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.dto.response.PlayerDtoResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import com.example.chess.mappers.GameInfoMapper;
import com.example.chess.mappers.PlayerMapper;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.PlayerService;
import com.example.chess.utils.PasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public Set<PlayerDtoResponse> getAllFriends(Long id) {
        Set<Player> friends = playerRepository.findAllFriends(id);
        return friends.stream().map(PlayerMapper::toDto).collect(Collectors.toSet());
    }

    @Override
    public Set<PlayerDtoResponse> getAllFriendsByName(String friendName, Long playerId) {
        Set<Player> friends = playerRepository.findFriendsByPlayerName(friendName, playerId);
        return friends.stream().map(PlayerMapper::toDto).collect(Collectors.toSet());
    }

    @Override
    public List<GameInfoDtoResponse> getAllGamesInfo(Long id) throws InvalidParamException {
        playerRepository.findById(id).orElseThrow(InvalidParamException::new);
        List<GameInfo> gamesInfo = Stream.concat(playerRepository.findAllGamesInfoAsWhitePlayer(id).stream(),
                playerRepository.findAllGamesInfoAsBlackPlayer(id).stream()).collect(Collectors.toList());
        List<GameInfo> sortedGamesInfo = gamesInfo.stream().
                sorted(Comparator.comparing(GameInfo::getStartTime)).collect(Collectors.toList());
        return sortedGamesInfo.stream().map(GameInfoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlayerDtoResponse sendFriendRequest(long senderId, String recipientEmail) throws InvalidParamException {
        Optional<Player> senderOpt = playerRepository.findById(senderId);
        Optional<Player> recipientOpt = playerRepository.findByEmail(recipientEmail);

        senderOpt.orElseThrow(InvalidParamException::new);
        recipientOpt.orElseThrow(InvalidParamException::new);


        Player sender = senderOpt.get();
        Player recipient = recipientOpt.get();

        if (sender.getFriends().contains(recipient)) {
            throw new InvalidParamException();
        }

        recipient.getFriendRequests().add(sender);

        try {
            PlayerDtoResponse playerDtoResponse = addFriend(senderId, recipientEmail);
        } catch (InvalidParamException e) {
            playerRepository.save(sender);
        }
        playerRepository.save(recipient);
        return PlayerMapper.toDto(recipient);
    }

    @Override
    public Set<PlayerDtoResponse> getFriendRequests(long id) throws InvalidParamException {
        playerRepository.findById(id).orElseThrow(InvalidParamException::new);
        return playerRepository.findAllFriendsRequests(id).stream().map(PlayerMapper::toDto).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public PlayerDtoResponse addFriend(Long senderId, String recipientEmail) throws InvalidParamException {
        Optional<Player> senderOpt = playerRepository.findById(senderId);
        Optional<Player> recipientOpt = playerRepository.findByEmail(recipientEmail);

        senderOpt.orElseThrow(InvalidParamException::new);
        recipientOpt.orElseThrow(InvalidParamException::new);

        Player sender = senderOpt.get();
        Player recipient = recipientOpt.get();
        if (!recipient.getFriendRequests().contains(sender) || !sender.getFriendRequests().contains(recipient)) {
            throw new InvalidParamException(); // переписать новый тип исключения
        }
        sender.getFriendRequests().remove(recipient);
        recipient.getFriendRequests().remove(sender);
        sender.getFriends().add(recipient);
        recipient.getFriends().add(sender);
        return PlayerMapper.toDto(recipient);
    }

    @Override
    @Transactional
    public PlayerDtoResponse deleteFriend(long playerId, String friendEmail) throws InvalidParamException {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        Optional<Player> friendOpt = playerRepository.findByEmail(friendEmail);
        playerOpt.orElseThrow(InvalidParamException::new);
        friendOpt.orElseThrow(InvalidParamException::new);
        if (!friendOpt.get().getFriends().contains(playerOpt.get())) {
            throw new InvalidParamException();
        }
        playerOpt.get().getFriends().remove(friendOpt.get());
        friendOpt.get().getFriends().remove(playerOpt.get());
        return PlayerMapper.toDto(friendOpt.get());
    }
}
