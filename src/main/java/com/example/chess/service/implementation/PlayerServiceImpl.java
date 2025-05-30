package com.example.chess.service.implementation;

import com.example.chess.dto.request.PlayerFilterRequest;
import com.example.chess.dto.request.PlayerRequest;
import com.example.chess.dto.request.PlayerUpdateRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.dto.response.PlayerResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import com.example.chess.exception.ConflictException;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.mappers.GameInfoMapper;
import com.example.chess.mappers.PlayerMapper;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.PlayerService;
import com.example.chess.utils.PasswordUtil;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private static final String PLAYER_NOT_FOUND_MSG = "Player not found";

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public PlayerResponse authenticatePlayer(String email, String password) {
        Optional<Player> playerOpt = playerRepository.findByEmail(email);
        Player player = playerOpt.orElseThrow(
                () -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));

        if (!PasswordUtil.matchPassword(password, player.getHashPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return PlayerMapper.toDto(player);
    }

    @Override
    public PlayerResponse getPlayerById(long id) throws ResourceNotFoundException {
        Player player = playerRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        return PlayerMapper.toDto(player);
    }

    @Override
    public List<PlayerResponse> getPlayersByNameAndEmail(String name, String email)
            throws ResourceNotFoundException {
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
            throw new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG);
        }

        return players.stream().map(PlayerMapper::toDto).toList();
    }

    @Override
    public PlayerResponse createPlayer(Player player) throws ConflictException {
        if (playerRepository.findByEmail(player.getEmail()).isPresent()) {
            throw new ConflictException("Player already exists");
        }
        player.setHashPassword(PasswordUtil.hashPassword(player.getHashPassword()));
        playerRepository.save(player);
        return PlayerMapper.toDto(player);
    }

    @Override
    public Set<PlayerResponse> getAllFriends(Long id) throws ResourceNotFoundException {
        Set<Player> friends = playerRepository.findAllFriends(id);
        playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        return friends.stream().map(PlayerMapper::toDto).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public List<GameInfoResponse> getAllGamesInfo(Long id) throws ResourceNotFoundException {
        Player player;
        player = playerRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        if (player.getGamesAsWhitePlayer() == null && player.getGamesAsBlackPlayer() == null) {
            throw new ResourceNotFoundException("Games not exists");
        }
        Stream<GameInfo> whiteGames = player.getGamesAsWhitePlayer() != null
                ? player.getGamesAsWhitePlayer().stream()
                : Stream.empty();
        Stream<GameInfo> blackGames = player.getGamesAsBlackPlayer() != null
                ? player.getGamesAsBlackPlayer().stream()
                : Stream.empty();

        List<GameInfo> gamesInfo = Stream.concat(whiteGames, blackGames)
                .sorted(Comparator.comparing(GameInfo::getStartTime))
                .toList();
        return gamesInfo.stream().map(GameInfoMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PlayerResponse sendFriendRequest(long senderId, String recipientEmail)
            throws ConflictException, ResourceNotFoundException {
        Optional<Player> senderOpt = playerRepository.findById(senderId);
        Optional<Player> recipientOpt = playerRepository.findByEmail(recipientEmail);

        senderOpt.orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        recipientOpt.orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));


        Player sender = senderOpt.get();
        Player recipient = recipientOpt.get();

        if (sender.getFriends().contains(recipient)) {
            throw new ConflictException("You are already friend");
        }

        recipient.getFriendRequests().add(sender);
        logger.info("Sent friend request to " + recipient.getEmail());

        if (recipient.getFriendRequests().contains(sender) && sender.getFriendRequests().contains(recipient)) {
            sender.getFriendRequests().remove(recipient);
            recipient.getFriendRequests().remove(sender);
            sender.getFriends().add(recipient);
            recipient.getFriends().add(sender);
            logger.info("Friend request added");
        }
        playerRepository.save(recipient);
        logger.info("Sent friend request to " + recipient.getEmail());
        playerRepository.save(sender);
        return PlayerMapper.toDto(recipient);
    }

    @Override
    @Transactional
    public Set<PlayerResponse> getFriendRequests(long id) throws ResourceNotFoundException {
        Player player;
        player = playerRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        return player.getFriendRequests().stream()
                .map(PlayerMapper::toDto).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public PlayerResponse addFriend(Long senderId, String recipientEmail)
            throws ConflictException, ResourceNotFoundException {
        Optional<Player> senderOpt = playerRepository.findById(senderId);
        Optional<Player> recipientOpt = playerRepository.findByEmail(recipientEmail);

        senderOpt.orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        recipientOpt.orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Player sender = senderOpt.get();
        Player recipient = recipientOpt.get();
        recipient.getFriendRequests().remove(sender);
        sender.getFriendRequests().remove(recipient);
        sender.getFriends().add(recipient);
        recipient.getFriends().add(sender);
        playerRepository.save(recipient);
        playerRepository.save(sender);
        return PlayerMapper.toDto(recipient);
    }

    @Override
    @Transactional
    public PlayerResponse deleteFriend(long playerId, String friendEmail)
            throws ConflictException, ResourceNotFoundException {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        Optional<Player> friendOpt = playerRepository.findByEmail(friendEmail);
        playerOpt.orElseThrow(() -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        friendOpt.orElseThrow(() -> new ResourceNotFoundException("Friend not found"));
        Player friend = friendOpt.get();
        Player player = playerOpt.get();
        if (!friend.getFriends().contains(playerOpt.get())) {
            throw new ConflictException("You are not friends");
        }
        player.getFriends().remove(friendOpt.get());
        friend.getFriends().remove(playerOpt.get());
        return PlayerMapper.toDto(friend);
    }

    @Override
    @Transactional
    public PlayerResponse deletePlayerById(long id)
            throws ResourceNotFoundException {
        Player player = playerRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        player.setFriends(null);
        player.setFriendRequests(null);

        playerRepository.deleteFriendshipsByPlayerId(id);
        playerRepository.deleteFriendRequestsByPlayerId(id);
        playerRepository.delete(player);
        return PlayerMapper.toDto(player);
    }

    @Override
    @Transactional
    public PlayerResponse updatePlayerById(long id, PlayerUpdateRequest playerRequest)
            throws ResourceNotFoundException {
        Player player = playerRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));
        player.setName(playerRequest.getName());
        player.setEmail(playerRequest.getEmail());
        playerRepository.save(player);
        logger.info("Player updated");
        return PlayerMapper.toDto(player);
    }

    @Override
    public Page<PlayerResponse> getPlayersByFilters(PlayerFilterRequest filter)
            throws ResourceNotFoundException {

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
                throw new ResourceNotFoundException("No players found");
            }

            return playerPage.map(PlayerMapper::toDto);

        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("No players found");
        }
    }

    @Override
    @Transactional
    public List<PlayerResponse> processBulkFriendRequests(
            long playerId,
            List<String> requestEmails)
            throws ResourceNotFoundException, ConflictException {

        List<Player> requestSenders = playerRepository.findByEmailIn(requestEmails);

        // Проверка на отсутствующих отправителей
        if (requestSenders.size() != requestEmails.size()) {
            Set<String> foundEmails = requestSenders.stream()
                    .map(Player::getEmail)
                    .collect(Collectors.toSet());

            List<String> missingEmails = requestEmails.stream()
                    .filter(email -> !foundEmails.contains(email))
                    .toList();

            throw new ResourceNotFoundException("Request senders not found: "
                    + String.join(", ", missingEmails));
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MSG));

        List<Player> invalidRequests = requestSenders.stream()
                .filter(sender -> !player.getFriendRequests().contains(sender))
                .toList();

        if (!invalidRequests.isEmpty()) {
            throw new ConflictException("No pending requests from: "
                    + invalidRequests.stream()
                    .map(Player::getEmail).collect(Collectors.joining(", ")));
        }

        requestSenders.forEach(sender -> {
            player.getFriendRequests().remove(sender);
            player.getFriends().add(sender);
            sender.getFriends().add(player);
        });

        playerRepository.saveAll(requestSenders);
        playerRepository.save(player);

        return requestSenders.stream()
                .map(PlayerMapper::toDto)
                .toList();
    }
}
