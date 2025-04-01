package com.example.chess.service;

import com.example.chess.dto.request.PlayerDtoRequest;
import com.example.chess.dto.request.PlayerFilterRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.dto.response.PlayerDtoResponse;
import com.example.chess.entity.Player;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface PlayerService {
    public PlayerDtoResponse getPlayerById(long id)
            throws NotFoundException;

    public Player getCachedPlayerById(long id) throws NotFoundException;

    public List<PlayerDtoResponse> getPlayersByNameAndEmail(String name, String email)
            throws NotFoundException;

    public PlayerDtoResponse createPlayer(Player player) throws InvalidParamException;

    public Set<PlayerDtoResponse> getAllFriends(Long playerId);

    public Set<PlayerDtoResponse> getAllFriendsByName(String friendName, Long playerId);

    public List<GameInfoDtoResponse> getAllGamesInfo(Long id)
            throws InvalidParamException;

    public PlayerDtoResponse addFriend(Long senderId, String recipientEmail)
            throws InvalidParamException;

    public PlayerDtoResponse sendFriendRequest(long playerId, String friendEmail)
            throws InvalidParamException;

    public Set<PlayerDtoResponse> getFriendRequests(long id)
            throws InvalidParamException;

    public PlayerDtoResponse deleteFriend(long playerId, String friendEmail)
            throws InvalidParamException;

    public PlayerDtoResponse deletePlayerById(long id)
            throws NotFoundException;

    public PlayerDtoResponse updatePlayerById(long id, PlayerDtoRequest playerDtoRequest)
            throws InvalidParamException;

    public Page<PlayerDtoResponse> getPlayersByFilters(PlayerFilterRequest filter);
}
