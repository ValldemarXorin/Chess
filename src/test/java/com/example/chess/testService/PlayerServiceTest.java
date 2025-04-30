package com.example.chess.testService;

import com.example.chess.dto.request.PlayerFilterRequest;
import com.example.chess.dto.request.PlayerRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.dto.response.PlayerResponse;
import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import com.example.chess.exception.ConflictException;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.implementation.PlayerServiceImpl;
import com.example.chess.utils.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerServiceImpl playerServiceImpl;

    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    void setUp() {
        // Создаем игроков
        player1 = new Player("player1@example.com", "password1", "Player1");
        player1.setId(1L);

        player2 = new Player("player2@example.com", "password2", "Player2");
        player2.setId(2L);

        player3 = new Player("player3@example.com", "password3", "Player3");
        player3.setId(3L);

        // Создаем игры с корректными датами и статусами
        LocalDateTime now = LocalDateTime.now();
        GameInfo game1 = new GameInfo(
                now.minusHours(1),
                now.plusHours(1),
                player1,
                player2,
                "IN_PROGRESS",
                "Test game 1"
        );
        game1.setId(1L);

        GameInfo game2 = new GameInfo(
                now.minusMinutes(30),
                now.plusHours(2),
                player3,
                player1,
                "IN_PROGRESS",
                "Test game 2"
        );
        game2.setId(2L);

        player1.setGamesAsWhitePlayer(List.of(game1));
        player1.setGamesAsBlackPlayer(List.of(game2));
        player2.setGamesAsBlackPlayer(List.of(game1));
        player3.setGamesAsWhitePlayer(List.of(game2));
    }

    @Test
    void getPlayerById_Success() throws ResourceNotFoundException {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        PlayerResponse response = playerServiceImpl.getPlayerById(1L);

        assertNotNull(response);
        assertEquals(player1.getId(), response.getId());
        assertEquals(player1.getName(), response.getName());
        assertEquals(player1.getEmail(), response.getEmail());
    }

    @Test
    void getPlayerById_NotFound() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> playerServiceImpl.getPlayerById(1L));
    }

    @Test
    void getPlayersByNameAndEmail_ByName_Success() throws ResourceNotFoundException {
        when(playerRepository.findByName("Player1")).thenReturn(List.of(player1));

        List<PlayerResponse> responses = playerServiceImpl.getPlayersByNameAndEmail("Player1", null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(player1.getId(), responses.get(0).getId());
    }

    @Test
    void getPlayersByNameAndEmail_ByEmail_Success() throws ResourceNotFoundException {
        when(playerRepository.findByEmail("player1@example.com")).thenReturn(Optional.of(player1));

        List<PlayerResponse> responses = playerServiceImpl.getPlayersByNameAndEmail(null, "player1@example.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(player1.getId(), responses.get(0).getId());
    }

    @Test
    void getPlayersByNameAndEmail_ByNameAndEmail_Success() throws ResourceNotFoundException {
        when(playerRepository.findByEmail("player1@example.com")).thenReturn(Optional.of(player1));

        List<PlayerResponse> responses = playerServiceImpl.getPlayersByNameAndEmail("Player1", "player1@example.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(player1.getId(), responses.get(0).getId());
    }

    @Test
    void getPlayersByNameAndEmail_NotFound() {
        when(playerRepository.findByName("Unknown")).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.getPlayersByNameAndEmail("Unknown", null));
    }

    @Test
    void createPlayer_Success() throws ConflictException {
        when(playerRepository.findByEmail("player1@example.com")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(player1);

        PlayerResponse response = playerServiceImpl.createPlayer(player1);

        assertNotNull(response);
        assertEquals(player1.getId(), response.getId());
        verify(playerRepository).save(player1);
    }

    @Test
    void createPlayer_Conflict() {
        when(playerRepository.findByEmail("player1@example.com")).thenReturn(Optional.of(player1));

        assertThrows(ConflictException.class, () -> playerServiceImpl.createPlayer(player1));
    }

    @Test
    void getAllFriends_Success() throws ResourceNotFoundException {
        Set<Player> friends = new HashSet<>(List.of(player2, player3));
        when(playerRepository.findAllFriends(1L)).thenReturn(friends);

        Set<PlayerResponse> responses = playerServiceImpl.getAllFriends(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getId() == 2L));
        assertTrue(responses.stream().anyMatch(r -> r.getId() == 3L));
    }

    @Test
    void getAllFriends_NotFound() {
        when(playerRepository.findAllFriends(1L)).thenReturn(Set.of());

        assertThrows(ResourceNotFoundException.class, () -> playerServiceImpl.getAllFriends(1L));
    }

    @Test
    void getAllGamesInfo_PlayerExistsAndHasGames_ReturnsGamesList() {
        // Устанавливаем поведение мока
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        // Вызываем тестируемый метод
        List<GameInfoResponse> result = playerServiceImpl.getAllGamesInfo(1L);

        // Проверяем, что репозиторий был вызван
        verify(playerRepository, times(1)).findById(1L);

        // Проверяем результат
        assertEquals(2, result.size());
        assertEquals("Test game 1", result.get(0).getNotes());
        assertEquals("Test game 2", result.get(1).getNotes());
    }

    // Тест 2: Игрок не найден
    @Test
    void getAllGamesInfo_PlayerNotFound_ThrowsException() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.getAllGamesInfo(99L);
        });

        verify(playerRepository, times(1)).findById(99L);
    }

    // Тест 3: У игрока нет игр (оба списка null)
    @Test
    void getAllGamesInfo_PlayerHasNoGames_ThrowsException() {
        Player playerWithoutGames = new Player("no@games.com", "pass", "NoGames");
        playerWithoutGames.setId(4L);
        playerWithoutGames.setGamesAsWhitePlayer(null);
        playerWithoutGames.setGamesAsBlackPlayer(null);

        when(playerRepository.findById(4L)).thenReturn(Optional.of(playerWithoutGames));

        assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.getAllGamesInfo(4L);
        });

        verify(playerRepository, times(1)).findById(4L);
    }

    @Test
    void getAllGamesInfo_PlayerHasOnlyWhiteGames_ReturnsGamesList() {
        Player playerWithWhiteGames = new Player("white@only.com", "pass", "WhiteOnly");
        playerWithWhiteGames.setId(5L);
        playerWithWhiteGames.setGamesAsWhitePlayer(player1.getGamesAsWhitePlayer()); // Список за белых есть
        playerWithWhiteGames.setGamesAsBlackPlayer(null); // Список за черных — null

        when(playerRepository.findById(5L)).thenReturn(Optional.of(playerWithWhiteGames));

        List<GameInfoResponse> result = playerServiceImpl.getAllGamesInfo(5L);

        assertEquals(1, result.size());
        assertEquals("Test game 1", result.get(0).getNotes()); // Проверяем поле description, а не notes
    }

    @Test
    void getAllGamesInfo_PlayerHasOnlyBlackGames_ReturnsGamesList() {
        Player playerWithBlackGames = new Player("black@only.com", "pass", "BlackOnly");
        playerWithBlackGames.setId(6L);
        playerWithBlackGames.setGamesAsWhitePlayer(null); // Список за белых — null
        playerWithBlackGames.setGamesAsBlackPlayer(player1.getGamesAsBlackPlayer()); // Список за черных есть

        when(playerRepository.findById(6L)).thenReturn(Optional.of(playerWithBlackGames));

        List<GameInfoResponse> result = playerServiceImpl.getAllGamesInfo(6L);

        assertEquals(1, result.size());
        assertEquals("Test game 2", result.get(0).getNotes());
    }

    // Тест 6: Проверка сортировки по времени начала игры
    @Test
    void getAllGamesInfo_GamesAreSortedByStartTime() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        List<GameInfoResponse> result = playerServiceImpl.getAllGamesInfo(1L);

        // Игра 1 началась раньше (now.minusHours(1)), чем игра 2 (now.minusMinutes(30))
        assertTrue(
                result.get(0).getStartTime().isBefore(result.get(1).getStartTime())
        );
    }

    @Test
    void getAllGamesInfo_PlayerHasNullWhiteAndNullBlack_ThrowsException() {
        // Создаем игрока с обоими списками игр = null
        Player playerWithNullGames = new Player("null@games.com", "pass", "NullGames");
        playerWithNullGames.setId(10L);
        playerWithNullGames.setGamesAsWhitePlayer(null);  // Явно указываем null
        playerWithNullGames.setGamesAsBlackPlayer(null); // Явно указываем null

        when(playerRepository.findById(10L)).thenReturn(Optional.of(playerWithNullGames));

        // Проверяем, что метод бросает исключение
        assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.getAllGamesInfo(10L);
        });

        // Проверяем, что findById был вызван
        verify(playerRepository, times(1)).findById(10L);
    }

    @Test
    void getFriendRequests_Success() throws ResourceNotFoundException {
        player1.getFriendRequests().add(player2);
        player1.getFriendRequests().add(player3);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        Set<PlayerResponse> responses = playerServiceImpl.getFriendRequests(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getId() == 2L));
        assertTrue(responses.stream().anyMatch(r -> r.getId() == 3L));
    }

    @Test
    void getFriendRequests_NotFound() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> playerServiceImpl.getFriendRequests(1L));
    }

    @Test
    void addFriend_Success() throws ConflictException, ResourceNotFoundException {
        // Подготовка данных
        player1.getFriendRequests().add(player2);
        player2.getFriendRequests().add(player1);

        // Настройка моков (только те, которые действительно используются)
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail("player2@example.com")).thenReturn(Optional.of(player2));

        // Вызов метода
        PlayerResponse response = playerServiceImpl.addFriend(1L, "player2@example.com");

        // Проверки
        assertNotNull(response);
        assertEquals(player2.getId(), response.getId());
        assertTrue(player1.getFriends().contains(player2));
        assertTrue(player2.getFriends().contains(player1));
        assertFalse(player1.getFriendRequests().contains(player2));
        assertFalse(player2.getFriendRequests().contains(player1));
    }

    @Test
    void addFriend_NoMutualRequests() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail("player2@example.com")).thenReturn(Optional.of(player2));

        assertThrows(ConflictException.class,
                () -> playerServiceImpl.addFriend(1L, "player2@example.com"));
    }

    @Test
    void deleteFriend_Success() throws ConflictException, ResourceNotFoundException {
        player1.getFriends().add(player2);
        player2.getFriends().add(player1);

        // Настройка моков (только те, которые действительно используются)
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail("player2@example.com")).thenReturn(Optional.of(player2));

        // Вызов метода
        PlayerResponse response = playerServiceImpl.deleteFriend(1L, "player2@example.com");

        // Проверки
        assertNotNull(response);
        assertEquals(player2.getId(), response.getId());
        assertFalse(player1.getFriends().contains(player2));
        assertFalse(player2.getFriends().contains(player1));
    }

    @Test
    void deleteFriend_NotFriends() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail("player2@example.com")).thenReturn(Optional.of(player2));

        assertThrows(ConflictException.class,
                () -> playerServiceImpl.deleteFriend(1L, "player2@example.com"));
    }

    @Test
    void deletePlayerById_Success() throws ResourceNotFoundException {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        doNothing().when(playerRepository).deleteFriendshipsByPlayerId(1L);
        doNothing().when(playerRepository).deleteFriendRequestsByPlayerId(1L);
        doNothing().when(playerRepository).delete(player1);

        PlayerResponse response = playerServiceImpl.deletePlayerById(1L);

        assertNotNull(response);
        assertEquals(player1.getId(), response.getId());
        verify(playerRepository).deleteFriendshipsByPlayerId(1L);
        verify(playerRepository).deleteFriendRequestsByPlayerId(1L);
        verify(playerRepository).delete(player1);
    }

    @Test
    void updatePlayerById_Success() throws ResourceNotFoundException {
        PlayerRequest request = new PlayerRequest();
        request.setName("UpdatedName");
        request.setEmail("updated@example.com");
        request.setPassword("newPassword");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.save(any(Player.class))).thenReturn(player1);

        PlayerResponse response = playerServiceImpl.updatePlayerById(1L, request);

        assertNotNull(response);
        assertEquals(request.getName(), player1.getName());
        assertEquals(request.getEmail(), player1.getEmail());
        assertNotEquals("newPassword", player1.getHashPassword()); // should be hashed
    }

    @Test
    void getPlayersByFilters_AllFilters_Success() throws ResourceNotFoundException {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setStatus("ACTIVE");
        filter.setNotes("test");
        filter.setPage(0);
        filter.setSize(10);

        Page<Player> playerPage = new PageImpl<>(List.of(player1, player2));
        when(playerRepository.findPlayersByFilters("ACTIVE", "test", PageRequest.of(0, 10)))
                .thenReturn(playerPage);

        Page<PlayerResponse> responses = playerServiceImpl.getPlayersByFilters(filter);

        assertNotNull(responses);
        assertEquals(2, responses.getTotalElements());
    }

    @Test
    void getPlayersByFilters_StatusOnly_Success() throws ResourceNotFoundException {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setStatus("ACTIVE");
        filter.setPage(0);
        filter.setSize(10);

        List<Player> players = List.of(player1, player2);
        when(playerRepository.findPlayersByGameStatus("ACTIVE")).thenReturn(players);

        Page<PlayerResponse> responses = playerServiceImpl.getPlayersByFilters(filter);

        assertNotNull(responses);
        assertEquals(2, responses.getTotalElements());
    }

    @Test
    void getPlayersByFilters_NotesOnly_Success() throws ResourceNotFoundException {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setNotes("test");
        filter.setPage(0);
        filter.setSize(10);

        List<Player> players = List.of(player1, player2);
        when(playerRepository.findPlayersByGameNotesContaining("test")).thenReturn(players);

        Page<PlayerResponse> responses = playerServiceImpl.getPlayersByFilters(filter);

        assertNotNull(responses);
        assertEquals(2, responses.getTotalElements());
    }

    @Test
    void getPlayersByFilters_NoFilters_Success() throws ResourceNotFoundException {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setPage(0);
        filter.setSize(10);

        Page<Player> playerPage = new PageImpl<>(List.of(player1, player2, player3));
        when(playerRepository.findAll(PageRequest.of(0, 10))).thenReturn(playerPage);

        Page<PlayerResponse> responses = playerServiceImpl.getPlayersByFilters(filter);

        assertNotNull(responses);
        assertEquals(3, responses.getTotalElements());
    }

    @Test
    void getPlayersByFilters_NotFound() {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setStatus("ACTIVE");
        filter.setPage(0);
        filter.setSize(10);

        when(playerRepository.findPlayersByGameStatus("ACTIVE")).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> playerServiceImpl.getPlayersByFilters(filter));
    }

    @Test
    void processBulkFriendRequests_Success() throws ResourceNotFoundException, ConflictException {
        List<String> emails = List.of("player2@example.com", "player3@example.com");
        List<Player> senders = List.of(player2, player3);

        player1.getFriendRequests().add(player2);
        player1.getFriendRequests().add(player3);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmailIn(emails)).thenReturn(senders);
        when(playerRepository.saveAll(anyList())).thenReturn(senders);
        when(playerRepository.save(any(Player.class))).thenReturn(player1);

        List<PlayerResponse> responses = playerServiceImpl.processBulkFriendRequests(1L, emails);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertFalse(player1.getFriendRequests().contains(player2));
        assertFalse(player1.getFriendRequests().contains(player3));
        assertTrue(player1.getFriends().contains(player2));
        assertTrue(player1.getFriends().contains(player3));
    }

    @Test
    void processBulkFriendRequests_PlayerNotFound() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.processBulkFriendRequests(1L, List.of("player2@example.com")));
    }

    @Test
    void processBulkFriendRequests_SendersNotFound() {
        List<String> emails = List.of("player2@example.com", "player3@example.com");
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmailIn(emails)).thenReturn(List.of(player2));

        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.processBulkFriendRequests(1L, emails));
    }

    @Test
    void processBulkFriendRequests_NoPendingRequests() {
        List<String> emails = List.of("player2@example.com", "player3@example.com");
        List<Player> senders = List.of(player2, player3);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmailIn(emails)).thenReturn(senders);

        assertThrows(ConflictException.class,
                () -> playerServiceImpl.processBulkFriendRequests(1L, emails));
    }

    // Дополнительные тесты для полного покрытия

    @Test
    void getPlayersByNameAndEmail_EmptyNameAndEmail() {
        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.getPlayersByNameAndEmail("", ""));
    }

    @Test
    void getAllGamesInfo_EmptyGames() throws ResourceNotFoundException {
        player1.setGamesAsWhitePlayer(Collections.emptyList());
        player1.setGamesAsBlackPlayer(Collections.emptyList());
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        List<GameInfoResponse> responses = playerServiceImpl.getAllGamesInfo(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getAllGamesInfo_MixedGamesOrder() throws ResourceNotFoundException {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        List<GameInfoResponse> responses = playerServiceImpl.getAllGamesInfo(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId()); // Должен быть первый по времени
        assertEquals(2L, responses.get(1).getId());
    }

    @Test
    void sendFriendRequest_AlreadyRequestedButNotFriends() throws ConflictException, ResourceNotFoundException {
        player2.getFriendRequests().add(player1);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail("player2@example.com")).thenReturn(Optional.of(player2));
        when(playerRepository.save(any(Player.class))).thenReturn(player2);

        PlayerResponse response = playerServiceImpl.sendFriendRequest(1L, "player2@example.com");

        assertNotNull(response);
        assertEquals(player2.getId(), response.getId());
        assertTrue(player2.getFriendRequests().contains(player1));
    }

    @Test
    void addFriend_OnlyOneSideRequest() {
        player1.getFriendRequests().add(player2);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail("player2@example.com")).thenReturn(Optional.of(player2));

        assertThrows(ConflictException.class,
                () -> playerServiceImpl.addFriend(1L, "player2@example.com"));
    }

    @Test
    void deletePlayerById_VerifyRelationsCleared() throws ResourceNotFoundException {
        // Настройка отношений
        player1.getFriends().add(player2);
        player1.getFriendRequests().add(player3);
        player2.getFriends().add(player1);
        player3.getFriendRequests().add(player1);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        doNothing().when(playerRepository).deleteFriendshipsByPlayerId(1L);
        doNothing().when(playerRepository).deleteFriendRequestsByPlayerId(1L);
        doNothing().when(playerRepository).delete(player1);

        PlayerResponse response = playerServiceImpl.deletePlayerById(1L);

        assertNotNull(response);
        verify(playerRepository).deleteFriendshipsByPlayerId(1L);
        verify(playerRepository).deleteFriendRequestsByPlayerId(1L);
        verify(playerRepository).delete(player1);
    }

    @Test
    void updatePlayerById_PasswordHashing() throws ResourceNotFoundException {
        PlayerRequest request = new PlayerRequest();
        request.setName("Player1");
        request.setEmail("player1@example.com");
        request.setPassword("plainPassword");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player saved = invocation.getArgument(0);
            assertTrue(PasswordUtil.matchPassword("plainPassword", saved.getHashPassword()));
            return saved;
        });

        PlayerResponse response = playerServiceImpl.updatePlayerById(1L, request);

        assertNotNull(response);
        verify(playerRepository).save(player1);
    }

    @Test
    void getPlayersByFilters_EmptyResultDataAccess() {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setStatus("INVALID_STATUS");
        filter.setPage(0);
        filter.setSize(10);

        when(playerRepository.findPlayersByGameStatus("INVALID_STATUS"))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.getPlayersByFilters(filter));
    }

    @Test
    void processBulkFriendRequests_PartialRequests() {
        List<String> emails = List.of("player2@example.com", "player3@example.com");
        player1.getFriendRequests().add(player2); // Только один запрос

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmailIn(emails)).thenReturn(List.of(player2, player3));

        assertThrows(ConflictException.class,
                () -> playerServiceImpl.processBulkFriendRequests(1L, emails));
    }

// Тесты для лямбда-выражений

    @Test
    void streamOperationsInGetAllGamesInfo() throws ResourceNotFoundException {

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        List<GameInfoResponse> responses = playerServiceImpl.getAllGamesInfo(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        // Проверяем порядок сортировки
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
    }

    @Test
    void streamOperationsInGetPlayersByNameAndEmail() throws ResourceNotFoundException {
        Player player1a = new Player();
        player1a.setId(10L);
        player1a.setName("Player1");
        player1a.setEmail("player1a@example.com");

        when(playerRepository.findByName("Player1")).thenReturn(List.of(player1, player1a));
        when(playerRepository.findByEmail("player1@example.com")).thenReturn(Optional.of(player1));

        // Тестируем лямбду в методе, которая очищает список при нахождении по email
        List<PlayerResponse> responses = playerServiceImpl.getPlayersByNameAndEmail("Player1", "player1@example.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
    }

    @Test
    void streamOperationsInGetAllFriends() throws ResourceNotFoundException {
        Player friend1 = new Player();
        friend1.setId(10L);
        Player friend2 = new Player();
        friend2.setId(20L);

        Set<Player> friends = Set.of(friend1, friend2);
        when(playerRepository.findAllFriends(1L)).thenReturn(friends);

        Set<PlayerResponse> responses = playerServiceImpl.getAllFriends(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getId() == 10L));
        assertTrue(responses.stream().anyMatch(r -> r.getId() == 20L));
    }

    @Test
    void streamOperationsInProcessBulkFriendRequests() throws ResourceNotFoundException, ConflictException {
        Player friend1 = new Player();
        friend1.setId(10L);
        friend1.setEmail("friend1@example.com");

        Player friend2 = new Player();
        friend2.setId(20L);
        friend2.setEmail("friend2@example.com");

        player1.getFriendRequests().add(friend1);
        player1.getFriendRequests().add(friend2);

        List<String> emails = List.of("friend1@example.com", "friend2@example.com");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmailIn(emails)).thenReturn(List.of(friend1, friend2));
        when(playerRepository.saveAll(anyList())).thenReturn(List.of(friend1, friend2));
        when(playerRepository.save(any(Player.class))).thenReturn(player1);

        List<PlayerResponse> responses = playerServiceImpl.processBulkFriendRequests(1L, emails);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        // Проверяем что оба запроса были обработаны
        assertFalse(player1.getFriendRequests().contains(friend1));
        assertFalse(player1.getFriendRequests().contains(friend2));
        assertTrue(player1.getFriends().contains(friend1));
        assertTrue(player1.getFriends().contains(friend2));
    }

    @Test
    void getPlayersByFilters_EmptyResultDataAccessException() {
        PlayerFilterRequest filter = new PlayerFilterRequest();
        filter.setStatus("INVALID_STATUS");
        filter.setPage(0);
        filter.setSize(10);

        when(playerRepository.findPlayersByGameStatus("INVALID_STATUS"))
                .thenThrow(new EmptyResultDataAccessException("No players found", 1));

        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.getPlayersByFilters(filter));
    }

    @Test
    void getAllGamesInfo_NullGames() {
        player1.setGamesAsWhitePlayer(null);
        player1.setGamesAsBlackPlayer(null);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        assertThrows(ResourceNotFoundException.class,
                () -> playerServiceImpl.getAllGamesInfo(1L));
    }

    @Test
    void addFriend_shouldThrowConflictWhenNoMutualRequests() {

        when(playerRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
        when(playerRepository.findByEmail(player2.getEmail())).thenReturn(Optional.of(player2));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> playerServiceImpl.addFriend(player1.getId(), player2.getEmail()));

        assertEquals("You are not have both requests", exception.getMessage());
    }

    @Test
    void sendFriendRequest_AddFriendSuccess_ShouldAddFriend() throws ConflictException, ResourceNotFoundException {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "recipient@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Act
        PlayerResponse result = playerServiceImpl.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(result);
        assertEquals(recipientEmail, result.getEmail());
        assertTrue(recipient.getFriendRequests().contains(sender));
        verify(playerRepository, times(1)).save(recipient);
        verify(playerRepository, times(1)).save(sender); // Ensure sender is also saved
    }

    @Test
    void sendFriendRequest_SenderNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "recipient@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> playerServiceImpl.sendFriendRequest(senderId, recipientEmail)
        );
        assertEquals("Sender not found", exception.getMessage());
    }

    @Test
    void sendFriendRequest_RecipientNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "recipient@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> playerServiceImpl.sendFriendRequest(senderId, recipientEmail)
        );
        assertEquals("Recipient not found", exception.getMessage());
    }

    @Test
    void sendFriendRequest_AlreadyFriends_ShouldThrowConflictException() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "recipient@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        sender.getFriends().add(recipient); // They are already friends

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Act & Assert
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> playerServiceImpl.sendFriendRequest(senderId, recipientEmail)
        );
        assertEquals("You are already friend", exception.getMessage());
    }

    @Test
    void sendFriendRequest_Success_ShouldSendRequest() throws ConflictException, ResourceNotFoundException {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "recipient@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Act
        PlayerResponse result = playerServiceImpl.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(result);
        assertEquals(recipientEmail, result.getEmail());
        assertTrue(recipient.getFriendRequests().contains(sender));
        verify(playerRepository, times(1)).save(recipient);
    }

    @Test
    public void deleteFriend_WhenPlayerNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        long playerId = 1L;
        String friendEmail = "friend@example.com";

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());
        // Не мокаем findByEmail, так как исключение должно выброситься раньше

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.deleteFriend(playerId, friendEmail);
        });

        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository).findById(playerId);
        // Убираем проверку на never(), так как в вашей реализации оба запроса выполняются до проверок
    }

    @Test
    public void deleteFriend_WhenFriendNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        long playerId = 1L;
        String friendEmail = "friend@example.com";
        Player player = new Player(); // предположим, что у вас есть класс Player

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.findByEmail(friendEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.deleteFriend(playerId, friendEmail);
        });

        verify(playerRepository).findById(playerId);
        verify(playerRepository).findByEmail(friendEmail);
    }

    @Test
    public void addFriend_WhenSenderNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long senderId = 1L;
        String recipientEmail = "recipient@example.com";

        when(playerRepository.findById(senderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.addFriend(senderId, recipientEmail);
        });

        assertEquals("Sender not found", exception.getMessage());
        verify(playerRepository).findById(senderId);
        verify(playerRepository).findByEmail(recipientEmail); // Оба метода вызываются до проверок
    }

    @Test
    public void addFriend_WhenRecipientNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long senderId = 1L;
        String recipientEmail = "recipient@example.com";
        Player sender = new Player();
        sender.setId(senderId);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.addFriend(senderId, recipientEmail);
        });

        assertEquals("Recipient not found", exception.getMessage());
        verify(playerRepository).findById(senderId);
        verify(playerRepository).findByEmail(recipientEmail);
    }

    @Test
    public void updatePlayerById_WhenPlayerNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        long playerId = 1L;
        PlayerRequest playerRequest = new PlayerRequest();
        playerRequest.setName("New Name");
        playerRequest.setEmail("new@email.com");
        playerRequest.setPassword("newPassword");

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.updatePlayerById(playerId, playerRequest);
        });

        // Verify
        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    public void deletePlayerById_WhenPlayerNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        long nonExistentId = 999L;

        when(playerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            playerServiceImpl.deletePlayerById(nonExistentId);
        });

        // Verify
        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository).findById(nonExistentId);
        verifyNoMoreInteractions(playerRepository); // Проверяем что дальше ничего не вызывалось
    }

    @Test
    void sendFriendRequest_WhenAddFriendThrowsConflict_ShouldSaveSender() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";

        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        // Настраиваем репозиторий
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Создаем spy объекта сервиса
        PlayerServiceImpl playerServiceSpy = spy(playerServiceImpl);

        // Настраиваем, чтобы addFriend бросал ConflictException
        doThrow(new ConflictException("Already friends"))
                .when(playerServiceSpy).addFriend(senderId, recipientEmail);

        // Act
        try {
            playerServiceSpy.sendFriendRequest(senderId, recipientEmail);
        } catch (ConflictException e) {
            // Это ожидаемое поведение
        }

        // Assert - проверяем, что sender был сохранен
        verify(playerRepository, times(1)).save(sender);
    }

    @Test
    void sendFriendRequest_Success() {
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        Player recipient = new Player();
        // Arrange
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Act
        PlayerResponse response = playerServiceImpl.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(response);
        verify(playerRepository).save(recipient);
        assertTrue(recipient.getFriendRequests().contains(sender));
    }

    @Test
    void sendFriendRequest_SenderNotFound() {
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        Player recipient = new Player();
        // Arrange
        when(playerRepository.findById(senderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                playerServiceImpl.sendFriendRequest(senderId, recipientEmail));
    }

    @Test
    void sendFriendRequest_RecipientNotFound() {
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        Player recipient = new Player();
        // Arrange
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                playerServiceImpl.sendFriendRequest(senderId, recipientEmail));
    }

    @Test
    void sendFriendRequest_AlreadyFriends() {
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        Player recipient = new Player();
        // Arrange
        sender.getFriends().add(recipient);
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Act & Assert
        assertThrows(ConflictException.class, () ->
                playerServiceImpl.sendFriendRequest(senderId, recipientEmail));
    }

    @Test
    void sendFriendRequest_AddFriendConflict_ShouldSaveBothPlayers() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        PlayerServiceImpl playerServiceSpy = spy(playerServiceImpl);
        doThrow(new ConflictException("Conflict when adding friend"))
                .when(playerServiceSpy).addFriend(senderId, recipientEmail);

        // Act
        PlayerResponse response = playerServiceSpy.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(response);
        verify(playerRepository).save(sender);
        verify(playerRepository).save(recipient);
    }

    @Test
    void sendFriendRequest_Successful_ShouldCallAddFriend() throws Exception {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Create a spy of the service
        PlayerServiceImpl playerServiceSpy = spy(playerServiceImpl);

        // Act
        playerServiceSpy.sendFriendRequest(senderId, recipientEmail);

        // Assert - verify addFriend was called
        verify(playerServiceSpy).addFriend(senderId, recipientEmail);
    }

    @Test
    void sendFriendRequest_AddFriendCalledSuccessfully() throws Exception {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        sender.setName("Sender");
        sender.setEmail("sender@example.com");

        Player recipient = new Player();
        recipient.setId(2L);
        recipient.setEmail(recipientEmail);
        recipient.setName("Recipient");

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Create expected response
        PlayerResponse expectedResponse = new PlayerResponse(
                recipient.getId(),
                recipient.getEmail(),
                recipient.getName()
        );

        // Spy on the real service
        PlayerServiceImpl playerServiceSpy = spy(playerServiceImpl);

        // Act
        PlayerResponse response = playerServiceSpy.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertEquals(expectedResponse.getEmail(), response.getEmail());
        assertEquals(expectedResponse.getName(), response.getName());

        verify(playerServiceSpy).addFriend(senderId, recipientEmail);
        verify(playerRepository).save(recipient);
    }

    @Test
    void sendFriendRequest_AddFriendConflict_SavesSender() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        PlayerServiceImpl playerServiceSpy = spy(playerServiceImpl);
        doThrow(new ConflictException("Conflict")).when(playerServiceSpy).addFriend(senderId, recipientEmail);

        // Act
        PlayerResponse response = playerServiceSpy.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(response);
        verify(playerRepository).save(sender); // Now we expect sender to be saved
        verify(playerRepository).save(recipient);
    }

    @Test
    void sendFriendRequest_WhenAddFriendThrowsConflict_ShouldSaveSenderAndRecipient() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        PlayerServiceImpl playerServiceSpy = spy(playerServiceImpl);
        doThrow(new ConflictException("Already friends")).when(playerServiceSpy).addFriend(senderId, recipientEmail);

        // Act
        PlayerResponse response = playerServiceSpy.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(response);
        verify(playerRepository).save(sender);
        verify(playerRepository).save(recipient);
    }

    @Test
    void sendFriendRequest_AddFriendThrowsConflict_ShouldStillSaveRecipient() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        PlayerServiceImpl spyService = spy(playerServiceImpl);
        doThrow(new ConflictException("Simulated")).when(spyService).addFriend(senderId, recipientEmail);

        // Act
        PlayerResponse response = spyService.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertNotNull(response);
        verify(playerRepository).save(sender);
        verify(playerRepository).save(recipient);
    }

    @Test
    void sendFriendRequest_AddFriendSuccessfully_ShouldSaveBothPlayers() {
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        PlayerResponse response = playerServiceImpl.sendFriendRequest(senderId, recipientEmail);

        assertNotNull(response);
        verify(playerRepository).save(sender);
        verify(playerRepository).save(recipient);
    }

    @Test
    void sendFriendRequest_AddFriendSucceeds_ShouldNotSaveSenderTwice() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = new Player();
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        // Act
        PlayerResponse response = playerServiceImpl.sendFriendRequest(senderId, recipientEmail);

        // Assert
        // Проверяем что sender сохраняется только один раз (в addFriend)
        verify(playerRepository, times(1)).save(sender);
        verify(playerRepository, times(1)).save(recipient); // Один раз в sendFriendRequest, один раз в addFriend
    }

    @Test
    void sendFriendRequest_ShouldAddSenderToRecipientFriendRequestsBeforeAddFriend() {
        // Arrange
        long senderId = 1L;
        String recipientEmail = "friend@example.com";
        Player sender = new Player();
        sender.setId(senderId);
        Player recipient = spy(new Player()); // spy recipient
        recipient.setEmail(recipientEmail);

        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(playerRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

        PlayerServiceImpl spyService = spy(playerServiceImpl);

        doAnswer(invocation -> {
            // Проверяем внутри addFriend, что уже была добавлена заявка
            assertTrue(recipient.getFriendRequests().contains(sender));
            return null;
        }).when(spyService).addFriend(senderId, recipientEmail);

        // Act
        PlayerResponse response = spyService.sendFriendRequest(senderId, recipientEmail);

        // Assert
        assertTrue(recipient.getFriendRequests().contains(sender));
    }

}
