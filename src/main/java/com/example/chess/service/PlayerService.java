package com.example.chess.service;

import com.example.chess.dto.response.PlayerDtoResponse;
import com.example.chess.entity.Player;
import com.example.chess.exception.InvalidParamException;
import com.example.chess.exception.NotFoundException;
import java.util.List;

public interface PlayerService {
    PlayerDtoResponse getPlayerById(long id) throws NotFoundException;
    List<PlayerDtoResponse> getPlayersByNameAndEmail(String name, String email) throws NotFoundException;
    PlayerDtoResponse createPlayer(Player player) throws InvalidParamException;
}
