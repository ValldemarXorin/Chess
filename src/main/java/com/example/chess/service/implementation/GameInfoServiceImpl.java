package com.example.chess.service.implementation;

import com.example.chess.dto.request.GameInfoDtoRequest;
import com.example.chess.dto.response.GameInfoDtoResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.exception.NotFoundException;
import com.example.chess.mappers.GameInfoMapper;
import com.example.chess.repository.GameInfoRepository;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.GameInfoService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GameInfoServiceImpl implements GameInfoService {
    private final GameInfoRepository gameInfoRepository;
    private final PlayerRepository playerRepository;

    public GameInfoServiceImpl(GameInfoRepository gameInfoRepository,
                               PlayerRepository playerRepository) {
        this.gameInfoRepository = gameInfoRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional
    public GameInfoDtoResponse createGame(GameInfoDtoRequest gameInfoDto) {
        GameInfo gameInfo = GameInfoMapper.toEntity(gameInfoDto, playerRepository);
        GameInfo savedGame = gameInfoRepository.save(gameInfo);
        return GameInfoMapper.toDto(savedGame);
    }

    @Override
    @Transactional
    public GameInfoDtoResponse getGameById(Long id) throws NotFoundException {
        GameInfo gameInfo = gameInfoRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        return GameInfoMapper.toDto(gameInfo);
    }

    @Override
    @Transactional
    public List<GameInfoDtoResponse> getAllGames() {
        return gameInfoRepository.findAll().stream()
                .map(GameInfoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public GameInfoDtoResponse updateGame(Long id, GameInfoDtoRequest gameInfoDto)
            throws NotFoundException {
        GameInfo existingGame = gameInfoRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        GameInfo updatedGame = GameInfoMapper.toEntity(gameInfoDto, playerRepository);
        updatedGame.setId(existingGame.getId());

        GameInfo savedGame = gameInfoRepository.save(updatedGame);
        return GameInfoMapper.toDto(savedGame);
    }

    @Override
    @Transactional
    public void deleteGame(Long id) throws NotFoundException {
        if (!gameInfoRepository.existsById(id)) {
            throw new NotFoundException();
        }
        gameInfoRepository.deleteById(id);
    }
}
