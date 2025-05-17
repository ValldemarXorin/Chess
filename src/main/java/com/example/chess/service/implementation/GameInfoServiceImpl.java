package com.example.chess.service.implementation;

import com.example.chess.dto.request.GameInfoRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.mappers.GameInfoMapper;
import com.example.chess.repository.GameInfoRepository;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.GameInfoService;
import com.example.chess.utils.Cache;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GameInfoServiceImpl implements GameInfoService {
    private final GameInfoRepository gameInfoRepository;
    private final PlayerRepository playerRepository;
    private final Cache<Long, GameInfo> cacheGameInfo;

    public GameInfoServiceImpl(GameInfoRepository gameInfoRepository,
                               PlayerRepository playerRepository,
                               Cache<Long, GameInfo> cacheGameInfo) {
        this.gameInfoRepository = gameInfoRepository;
        this.playerRepository = playerRepository;
        this.cacheGameInfo = cacheGameInfo;
    }

    @Override
    public GameInfo getCachedGameInfo(Long id) throws ResourceNotFoundException {
        if (cacheGameInfo.getValue(id) != null) {
            return cacheGameInfo.getValue(id);
        }

        GameInfo gameInfo = gameInfoRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("game info not found"));
        cacheGameInfo.putValue(id, gameInfo);
        return gameInfo;
    }

    @Override
    @Transactional
    public GameInfoResponse createGame(GameInfoRequest gameInfoDto) {
        GameInfo gameInfo = GameInfoMapper.toEntity(gameInfoDto, playerRepository);
        GameInfo savedGame = gameInfoRepository.save(gameInfo);
        return GameInfoMapper.toDto(savedGame);
    }

    @Override
    @Transactional
    public GameInfoResponse getGameById(Long id) {
        GameInfo gameInfo = getCachedGameInfo(id);
        return GameInfoMapper.toDto(gameInfo);
    }

    @Override
    @Transactional
    public List<GameInfoResponse> getAllGames() {
        return gameInfoRepository.findAll().stream()
                .map(GameInfoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public GameInfoResponse updateGame(Long id, GameInfoRequest gameInfoDto) {
        GameInfo existingGame = getCachedGameInfo(id);

        GameInfo updatedGame = GameInfoMapper.toEntity(gameInfoDto, playerRepository);
        updatedGame.setId(id);

        GameInfo savedGame = gameInfoRepository.save(updatedGame);
        cacheGameInfo.putValue(id, savedGame);
        return GameInfoMapper.toDto(savedGame);
    }

    @Override
    @Transactional
    public void deleteGame(Long id) throws ResourceNotFoundException {
        if (!gameInfoRepository.existsById(id)) {
            throw new ResourceNotFoundException("game info not found");
        }

        if (cacheGameInfo.getValue(id) != null) {
            cacheGameInfo.remove(id);
        }
        gameInfoRepository.deleteById(id);
    }
}
