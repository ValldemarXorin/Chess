package com.example.chess.service;

import com.example.chess.dto.request.PlayerFilterRequest;
import com.example.chess.dto.request.PlayerUpdateRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.dto.response.PlayerResponse;
import com.example.chess.entity.Player;
import com.example.chess.exception.ConflictException;
import com.example.chess.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;

public interface PlayerService {
    public PlayerResponse authenticatePlayer(String email, String password);

    public PlayerResponse getPlayerById(long id)
            throws ResourceNotFoundException;

    public List<PlayerResponse> getPlayersByNameAndEmail(String name, String email)
            throws ResourceNotFoundException;

    public PlayerResponse createPlayer(Player player)
            throws ConflictException;

    public Set<PlayerResponse> getAllFriends(Long playerId)
            throws ResourceNotFoundException;

    public List<GameInfoResponse> getAllGamesInfo(Long id)
            throws ResourceNotFoundException;

    public PlayerResponse addFriend(Long senderId, String recipientEmail)
            throws ConflictException;

    public PlayerResponse sendFriendRequest(long playerId, String friendEmail)
            throws ConflictException, ResourceNotFoundException;

    public Set<PlayerResponse> getFriendRequests(long id)
            throws ConflictException, ResourceNotFoundException;

    public PlayerResponse deleteFriend(long playerId, String friendEmail)
            throws ConflictException, ResourceNotFoundException;

    public PlayerResponse deletePlayerById(long id)
            throws ResourceNotFoundException;

    public PlayerResponse updatePlayerById(long id, PlayerUpdateRequest playerRequest)
            throws ResourceNotFoundException;

    public Page<PlayerResponse> getPlayersByFilters(PlayerFilterRequest filter)
            throws ResourceNotFoundException;

    public List<PlayerResponse> processBulkFriendRequests(long playerId,
            List<String> requestEmails)
            throws ResourceNotFoundException, ConflictException;
}
