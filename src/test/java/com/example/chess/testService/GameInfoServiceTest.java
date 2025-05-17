package com.example.chess.testService;

import com.example.chess.dto.request.GameInfoRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.dto.response.PlayerResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.mappers.GameInfoMapper;
import com.example.chess.mappers.PlayerMapper;
import com.example.chess.repository.GameInfoRepository;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.implementation.GameInfoServiceImpl;
import com.example.chess.utils.Cache;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameInfoServiceTest {

    @Mock
    private GameInfoRepository gameInfoRepository;

    @Mock
    private Cache<Long, GameInfo> cacheGameInfo;

    @InjectMocks
    private GameInfoServiceImpl gameInfoService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameInfoMapper gameInfoMapper;

    private final Long existingGameId = 1L;
    private final Long nonExistingGameId = 999L;
    private final GameInfo testGameInfo = new GameInfo();
    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusHours(1);

    @Test
    void getCachedGameInfo_WhenGameInCache_ShouldReturnFromCache() {
        // Arrange
        when(cacheGameInfo.getValue(existingGameId)).thenReturn(testGameInfo);

        // Act
        GameInfo result = gameInfoService.getCachedGameInfo(existingGameId);

        // Assert
        assertSame(testGameInfo, result, "Should return game from cache");
        verify(cacheGameInfo, times(2)).getValue(existingGameId);
        verifyNoInteractions(gameInfoRepository);
    }

    @Test
    void getCachedGameInfo_WhenGameNotInCacheButExistsInDb_ShouldFetchFromDbAndCache() {
        // Arrange
        when(cacheGameInfo.getValue(existingGameId)).thenReturn(null);
        when(gameInfoRepository.findById(existingGameId)).thenReturn(Optional.of(testGameInfo));

        // Act
        GameInfo result = gameInfoService.getCachedGameInfo(existingGameId);

        // Assert
        assertSame(testGameInfo, result, "Should return game from DB");
        verify(cacheGameInfo, times(1)).getValue(existingGameId);
        verify(gameInfoRepository, times(1)).findById(existingGameId);
        verify(cacheGameInfo, times(1)).putValue(existingGameId, testGameInfo);
    }

    @Test
    void getCachedGameInfo_WhenGameNotInCacheAndNotInDb_ShouldThrowException() {
        // Arrange
        when(cacheGameInfo.getValue(nonExistingGameId)).thenReturn(null);
        when(gameInfoRepository.findById(nonExistingGameId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gameInfoService.getCachedGameInfo(nonExistingGameId),
                "Should throw ResourceNotFoundException"
        );

        assertEquals("game info not found", exception.getMessage());
        verify(cacheGameInfo, times(1)).getValue(nonExistingGameId);
        verify(gameInfoRepository, times(1)).findById(nonExistingGameId);
        verify(cacheGameInfo, never()).putValue(any(), any());
    }

    @Test
    void getCachedGameInfo_WhenCacheReturnsNullButDbHasGame_ShouldHandleGracefully() {
        // Arrange
        when(cacheGameInfo.getValue(existingGameId)).thenReturn(null);
        when(gameInfoRepository.findById(existingGameId)).thenReturn(Optional.of(testGameInfo));

        // Act
        GameInfo result = gameInfoService.getCachedGameInfo(existingGameId);

        // Assert
        assertNotNull(result, "Should return game even when cache returned null");
        verify(cacheGameInfo).putValue(existingGameId, testGameInfo);
    }

    @Test
    @Transactional
    void deleteGame_WhenGameExists_ShouldDeleteFromCacheAndRepository() {
        // Arrange
        when(gameInfoRepository.existsById(existingGameId)).thenReturn(true);
        when(cacheGameInfo.getValue(existingGameId)).thenReturn(testGameInfo);

        // Act
        gameInfoService.deleteGame(existingGameId);

        // Assert
        verify(gameInfoRepository).existsById(existingGameId);
        verify(cacheGameInfo).getValue(existingGameId);
        verify(cacheGameInfo).remove(existingGameId);
        verify(gameInfoRepository).deleteById(existingGameId);
    }

    @Test
    @Transactional
    void deleteGame_WhenGameExistsButNotInCache_ShouldDeleteOnlyFromRepository() {
        // Arrange
        when(gameInfoRepository.existsById(existingGameId)).thenReturn(true);
        when(cacheGameInfo.getValue(existingGameId)).thenReturn(null);

        // Act
        gameInfoService.deleteGame(existingGameId);

        // Assert
        verify(gameInfoRepository).existsById(existingGameId);
        verify(cacheGameInfo).getValue(existingGameId);
        verify(cacheGameInfo, never()).remove(existingGameId);
        verify(gameInfoRepository).deleteById(existingGameId);
    }

    @Test
    @Transactional
    void deleteGame_WhenGameDoesNotExist_ShouldThrowException() {
        // Arrange
        when(gameInfoRepository.existsById(nonExistingGameId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gameInfoService.deleteGame(nonExistingGameId),
                "Should throw ResourceNotFoundException when game not found"
        );

        assertEquals("game info not found", exception.getMessage());
        verify(gameInfoRepository).existsById(nonExistingGameId);
        verifyNoInteractions(cacheGameInfo);
        verify(gameInfoRepository, never()).deleteById(any());
    }

    @Test
    void getAllGames_WhenNoGamesInDb_ShouldReturnEmptyList() {
        // Arrange
        when(gameInfoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<GameInfoResponse> result = gameInfoService.getAllGames();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return an empty list when no games are present");
        verify(gameInfoRepository, times(1)).findAll();
    }

    @Test
    void getGameById_WhenGameNotInCacheAndNotInDb_ShouldThrowException() {
        // Arrange
        when(cacheGameInfo.getValue(999L)).thenReturn(null);
        when(gameInfoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gameInfoService.getGameById(999L),
                "Should throw ResourceNotFoundException when game not found"
        );

        assertEquals("game info not found", exception.getMessage());
        verify(cacheGameInfo, times(1)).getValue(999L);
        verify(gameInfoRepository, times(1)).findById(999L);
        verify(cacheGameInfo, never()).putValue(any(), any());
    }

    @Test
    void createGame_shouldCreateAndReturnGameInfoResponse() {
        // given
        long whiteId = 1L;
        long blackId = 2L;
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 15, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 16, 0);

        GameInfoRequest request = new GameInfoRequest(start, end, "IN_PROGRESS", "Test notes", whiteId, blackId);

        Player whitePlayer = new Player("white@email.com", "hash1", "White");
        Player blackPlayer = new Player("black@email.com", "hash2", "Black");

        GameInfo gameEntity = new GameInfo();
        gameEntity.setStartTime(start);
        gameEntity.setEndTime(end);
        gameEntity.setStatus("IN_PROGRESS");
        gameEntity.setNotes("Test notes");
        gameEntity.setWhitePlayer(whitePlayer);
        gameEntity.setBlackPlayer(blackPlayer);

        GameInfo savedGame = new GameInfo();
        savedGame.setId(10L);
        savedGame.setStartTime(start);
        savedGame.setEndTime(end);
        savedGame.setStatus("IN_PROGRESS");
        savedGame.setNotes("Test notes");
        savedGame.setWhitePlayer(whitePlayer);
        savedGame.setBlackPlayer(blackPlayer);

        // mock map + repo
        try (MockedStatic<GameInfoMapper> mapperMock = Mockito.mockStatic(GameInfoMapper.class)) {
            mapperMock.when(() -> GameInfoMapper.toEntity(request, playerRepository))
                    .thenReturn(gameEntity);
            when(gameInfoRepository.save(gameEntity)).thenReturn(savedGame);
            GameInfoResponse expectedResponse = new GameInfoResponse(
                    10L, start, end, "IN_PROGRESS", "Test notes",
                    new PlayerResponse(1L, "white@email.com", "White"),
                    new PlayerResponse(2L, "black@email.com", "Black")
            );
            mapperMock.when(() -> GameInfoMapper.toDto(savedGame)).thenReturn(expectedResponse);

            // when
            GameInfoResponse actual = gameInfoService.createGame(request);

            // then
            assertNotNull(actual);
            assertEquals(expectedResponse, actual);
        }

        verify(gameInfoRepository, times(1)).save(any(GameInfo.class));
    }

    @Test
    void updateGame_shouldUpdateAndReturnGameInfoResponse() {
        // given
        long id = 42L;
        long whiteId = 1L;
        long blackId = 2L;
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 14, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 1, 15, 0);

        GameInfoRequest request = new GameInfoRequest(start, end, "FINISHED", "Updated notes", whiteId, blackId);

        Player whitePlayer = new Player("white@email.com", "hash1", "White");
        Player blackPlayer = new Player("black@email.com", "hash2", "Black");

        GameInfo existingGame = new GameInfo();
        existingGame.setId(id);
        existingGame.setWhitePlayer(whitePlayer);
        existingGame.setBlackPlayer(blackPlayer);

        GameInfo updatedEntity = new GameInfo();
        updatedEntity.setId(id);
        updatedEntity.setStartTime(start);
        updatedEntity.setEndTime(end);
        updatedEntity.setStatus("FINISHED");
        updatedEntity.setNotes("Updated notes");
        updatedEntity.setWhitePlayer(whitePlayer);
        updatedEntity.setBlackPlayer(blackPlayer);

        GameInfo savedGame = new GameInfo();
        savedGame.setId(id);
        savedGame.setStartTime(start);
        savedGame.setEndTime(end);
        savedGame.setStatus("FINISHED");
        savedGame.setNotes("Updated notes");
        savedGame.setWhitePlayer(whitePlayer);
        savedGame.setBlackPlayer(blackPlayer);

        GameInfoResponse expectedResponse = new GameInfoResponse(
                id, start, end, "FINISHED", "Updated notes",
                new PlayerResponse(whiteId, "white@email.com", "White"),
                new PlayerResponse(blackId, "black@email.com", "Black")
        );

        try (MockedStatic<GameInfoMapper> mapperMock = mockStatic(GameInfoMapper.class)) {
            // mock mapper
            mapperMock.when(() -> GameInfoMapper.toEntity(request, playerRepository))
                    .thenReturn(updatedEntity);
            mapperMock.when(() -> GameInfoMapper.toDto(savedGame))
                    .thenReturn(expectedResponse);

            // mock dependencies
            when(gameInfoRepository.findById(id)).thenReturn(Optional.of(existingGame));
            when(gameInfoRepository.save(updatedEntity)).thenReturn(savedGame);

            // when
            GameInfoResponse actual = gameInfoService.updateGame(id, request);

            // then
            assertNotNull(actual);
            assertEquals(expectedResponse, actual);
            assertEquals(id, updatedEntity.getId());

            verify(gameInfoRepository, times(1)).findById(id);
            verify(gameInfoRepository, times(1)).save(updatedEntity);
        }
    }

    @Test
    void getGameById_WhenGameInCache_ShouldReturnGameFromCache() {
        // Arrange
        Long id = 42L;
        Player whitePlayer = new Player("white@email.com", "hash1", "White");
        Player blackPlayer = new Player("black@email.com", "hash2", "Black");

        GameInfo cachedGameInfo = new GameInfo();
        cachedGameInfo.setId(id);
        cachedGameInfo.setStartTime(LocalDateTime.now());
        cachedGameInfo.setEndTime(LocalDateTime.now().plusHours(1));
        cachedGameInfo.setStatus("ongoing");
        cachedGameInfo.setNotes("Game in cache");
        cachedGameInfo.setWhitePlayer(whitePlayer);
        cachedGameInfo.setBlackPlayer(blackPlayer);

        when(cacheGameInfo.getValue(id)).thenReturn(cachedGameInfo);

        GameInfoResponse expectedResponse = new GameInfoResponse(
                id, cachedGameInfo.getStartTime(), cachedGameInfo.getEndTime(),
                cachedGameInfo.getStatus(), cachedGameInfo.getNotes(),
                new PlayerResponse(whitePlayer.getId(), whitePlayer.getEmail(), whitePlayer.getName()),
                new PlayerResponse(blackPlayer.getId(), blackPlayer.getEmail(), blackPlayer.getName())
        );

        // Act
        GameInfoResponse result = gameInfoService.getGameById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getStatus(), result.getStatus());
        verify(cacheGameInfo, times(2)).getValue(id);
        verifyNoInteractions(gameInfoRepository); // Ensure no DB interaction
    }
}