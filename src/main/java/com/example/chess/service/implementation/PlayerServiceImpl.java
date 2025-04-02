package com.example.chess.service.implementation;

import com.example.chess.dto.request.PlayerDtoRequest;
import com.example.chess.dto.request.PlayerFilterRequest;
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
import com.example.chess.utils.Cache;
import com.example.chess.utils.PasswordUtil;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final Cache<Long, Player> playerCache;

    public PlayerServiceImpl(PlayerRepository playerRepository,
                             Cache<Long, Player> playerCache) {
        this.playerRepository = playerRepository;
        this.playerCache = playerCache;
    }

    @Override
    public Player getCachedPlayerById(long id) throws NotFoundException {
        Player cachedPlayer = playerCache.getValue(id);
        if (cachedPlayer != null) {
            return cachedPlayer;
        }

        Player player = playerRepository.findById(id).orElseThrow(NotFoundException::new);

        playerCache.putValue(id, player);
        return player;
    }

    @Override
    public PlayerDtoResponse getPlayerById(long id) throws NotFoundException {
        Player player = getCachedPlayerById(id);
        return PlayerMapper.toDto(player);
    }

    @Override
    public List<PlayerDtoResponse> getPlayersByNameAndEmail(String name, String email)
            throws NotFoundException {
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
    @Transactional
    public List<GameInfoDtoResponse> getAllGamesInfo(Long id) throws InvalidParamException {
        try {
            getCachedPlayerById(id);
        } catch (NotFoundException e) {
            throw new InvalidParamException();
        }
        List<GameInfo> gamesInfo =
                Stream.concat(playerRepository.findAllGamesInfoAsWhitePlayer(id).stream(),
                playerRepository.findAllGamesInfoAsBlackPlayer(id).stream())
                        .toList();
        List<GameInfo> sortedGamesInfo = gamesInfo.stream()
                .sorted(Comparator.comparing(GameInfo::getStartTime)).toList();
        return sortedGamesInfo.stream().map(GameInfoMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PlayerDtoResponse sendFriendRequest(long senderId, String recipientEmail)
            throws InvalidParamException {
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
            addFriend(senderId, recipientEmail);
        } catch (InvalidParamException e) {
            playerRepository.save(sender);
        }
        playerRepository.save(recipient);
        return PlayerMapper.toDto(recipient);
    }

    @Override
    public Set<PlayerDtoResponse> getFriendRequests(long id) throws InvalidParamException {
        try {
            getCachedPlayerById(id);
        } catch (NotFoundException e) {
            throw new InvalidParamException();
        }
        return playerRepository.findAllFriendsRequests(id).stream().map(PlayerMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public PlayerDtoResponse addFriend(Long senderId, String recipientEmail)
            throws InvalidParamException {
        Optional<Player> senderOpt = playerRepository.findById(senderId);
        Optional<Player> recipientOpt = playerRepository.findByEmail(recipientEmail);

        senderOpt.orElseThrow(InvalidParamException::new);
        recipientOpt.orElseThrow(InvalidParamException::new);

        Player sender = senderOpt.get();
        Player recipient = recipientOpt.get();
        if (!recipient.getFriendRequests().contains(sender)
                || !sender.getFriendRequests().contains(recipient)) {
            throw new InvalidParamException(); // переписать новый тип исключения
        }
        if (recipient.getFriendRequests().contains(sender)) {
            recipient.getFriendRequests().remove(sender);
        }
        if (sender.getFriendRequests().contains(recipient)) {
            sender.getFriendRequests().remove(recipient);
        }
        sender.getFriends().add(recipient);
        recipient.getFriends().add(sender);
        return PlayerMapper.toDto(recipient);
    }

    @Override
    @Transactional
    public PlayerDtoResponse deleteFriend(long playerId, String friendEmail)
            throws InvalidParamException {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        Optional<Player> friendOpt = playerRepository.findByEmail(friendEmail);
        playerOpt.orElseThrow(InvalidParamException::new);
        friendOpt.orElseThrow(InvalidParamException::new);
        Player friend = friendOpt.get();
        Player player = playerOpt.get();
        if (!friend.getFriends().contains(playerOpt.get())) {
            throw new InvalidParamException();
        }
        player.getFriends().remove(friendOpt.get());
        friend.getFriends().remove(playerOpt.get());
        return PlayerMapper.toDto(friend);
    }

    @Override
    @Transactional
    public PlayerDtoResponse deletePlayerById(long id)
            throws NotFoundException {
        Optional<Player> playerOpt = playerRepository.findById(id);
        playerOpt.orElseThrow(NotFoundException::new);
        Player player = playerOpt.get();
        player.setFriends(null);
        player.setFriendRequests(null);

        playerRepository.deleteFriendshipsByPlayerId(id);
        playerRepository.deleteFriendRequestsByPlayerId(id);
        playerRepository.delete(player);
        if (playerCache.getValue(id) != null) {
            playerCache.remove(id);
        }
        return PlayerMapper.toDto(player);
    }

    @Override
    @Transactional
    public PlayerDtoResponse updatePlayerById(long id, PlayerDtoRequest playerDtoRequest)
        throws InvalidParamException {
        Optional<Player> playerOpt = playerRepository.findById(id);
        playerOpt.orElseThrow(InvalidParamException::new);
        Player player = playerOpt.get();
        player.setName(playerDtoRequest.getName());
        player.setEmail(playerDtoRequest.getEmail());
        player.setHashPassword(PasswordUtil.hashPassword(playerDtoRequest.getPassword()));
        playerRepository.save(player);
        playerCache.putValue(id, player);
        return PlayerMapper.toDto(player);
    }

    @Override
    public Page<PlayerDtoResponse> getPlayersByFilters(PlayerFilterRequest filter)
            throws NotFoundException {

        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<Player> playerPage;

        try {
            if (filter.getStatus() != null && filter.getNotes() != null) {
                playerPage = playerRepository.findPlayersByFilters(
                        filter.getStatus(),
                        filter.getNotes(),
                        pageable
                );
            } else if (filter.getStatus() != null) {
                List<Player> players = playerRepository.findPlayersByGameStatus(filter.getStatus());
                playerPage = new PageImpl<>(players, pageable, players.size());
            } else if (filter.getNotes() != null) {
                List<Player> players = playerRepository
                        .findPlayersByGameNotesContaining(filter.getNotes());
                playerPage = new PageImpl<>(players, pageable, players.size());
            } else {
                playerPage = playerRepository.findAll(pageable);
            }

            if (playerPage.isEmpty()) {
                throw new NotFoundException();
            }

            return playerPage.map(PlayerMapper::toDto);

        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException();
        }
    }
}
