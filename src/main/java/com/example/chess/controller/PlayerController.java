package com.example.chess.controller;

import com.example.chess.dto.request.PlayerRequest;
import com.example.chess.dto.request.PlayerFilterRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.dto.response.PlayerResponse;
import com.example.chess.entity.Player;
import com.example.chess.mappers.PlayerMapper;
import com.example.chess.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Player Management", description = "Endpoints for managing chess players and their relationships")
@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Operation(summary = "Get player by ID", description = "Retrieves detailed information about a specific player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> findPlayerById(
            @Parameter(description = "ID of the player to retrieve", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(playerService.getPlayerById(id));
    }

    @Operation(summary = "Search players", description = "Finds players by name and/or email (both optional)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Players found successfully")
    })
    @GetMapping
    public ResponseEntity<List<PlayerResponse>> findPlayersByNameAndEmail(
            @Parameter(description = "Player name to search for", example = "John")
            @RequestParam(required = false) String name,
            @Parameter(description = "Player email to search for", example = "john@example.com")
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(playerService.getPlayersByNameAndEmail(name, email));
    }

    @Operation(summary = "Create new player", description = "Registers a new player in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Player data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PlayerRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"name\": \"John Doe\", \"email\": \"john@example.com\", \"rating\": 1500}"
                            )
                    )
            )
            @Valid @RequestBody PlayerRequest playerRequest) {
        return ResponseEntity.ok(playerService.createPlayer(PlayerMapper.toEntity(playerRequest)));
    }

    @Operation(summary = "Get player's games", description = "Retrieves information about all games of a specific player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{id}/gamesInfo")
    public ResponseEntity<List<GameInfoResponse>> getGamesInfo(
            @Parameter(description = "ID of the player", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(playerService.getAllGamesInfo(id));
    }

    @Operation(summary = "Get player's friends", description = "Retrieves all friends of a specific player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friends retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<PlayerResponse>> getFriends(
            @Parameter(description = "ID of the player", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(playerService.getAllFriends(id));
    }

    @Operation(summary = "Send friend request", description = "Sends a friend request from one player to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email or self-friending attempt"),
            @ApiResponse(responseCode = "404", description = "Player or friend not found")
    })
    @PostMapping("/{id}/send_friend_request")
    public ResponseEntity<PlayerResponse> sendFriendRequest(
            @Parameter(description = "ID of the sender player", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email of the player to send request to",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "\"friend@example.com\""))
            )
            @RequestBody String friendEmail) {
        friendEmail = friendEmail.replace("\"", "");
        return ResponseEntity.ok(playerService.sendFriendRequest(id, friendEmail));
    }

    @Operation(summary = "Get friend requests", description = "Retrieves all pending friend requests for a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend requests retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{id}/get_friend_request")
    public ResponseEntity<Set<PlayerResponse>> getFriendRequest(
            @Parameter(description = "ID of the player", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(playerService.getFriendRequests(id));
    }

    @Operation(summary = "Remove friend", description = "Removes a friend from player's friend list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email"),
            @ApiResponse(responseCode = "404", description = "Player or friend not found")
    })
    @DeleteMapping("/{id}/remove_friend")
    public ResponseEntity<PlayerResponse> removeFriend(
            @Parameter(description = "ID of the player", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email of the friend to remove",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "\"friend@example.com\""))
            )
            @RequestBody String friendEmail) {
        friendEmail = friendEmail.replace("\"", "");
        return ResponseEntity.ok(playerService.deleteFriend(id, friendEmail));
    }

    @Operation(summary = "Delete player", description = "Removes a player from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<PlayerResponse> removePlayer(
            @Parameter(description = "ID of the player to delete", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(playerService.deletePlayerById(id));
    }

    @Operation(summary = "Update player", description = "Updates player's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @Parameter(description = "ID of the player to update", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated player data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PlayerRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"name\": \"John Doe Updated\", \"email\": \"john.updated@example.com\", \"rating\": 1600}"
                            )
                    )
            )
            @RequestBody PlayerRequest playerRequest) {
        return ResponseEntity.ok(playerService.updatePlayerById(id, playerRequest));
    }

    @Operation(summary = "Filter players", description = "Searches players with pagination and filtering options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Players filtered successfully")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<PlayerResponse>> filterPlayers(
            @Parameter(description = "Filter criteria for players")
            @ModelAttribute PlayerFilterRequest filter) {
        return ResponseEntity.ok(playerService.getPlayersByFilters(filter));
    }

    @Operation(summary = "Approve friend request",
            description = "Approves a pending friend request from another player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request approved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email"),
            @ApiResponse(responseCode = "404", description = "Player or friend not found")
    })
    @PostMapping("/{senderId}/aproove_request")
    public ResponseEntity<PlayerResponse> approveFriendRequest(
            @Parameter(description = "ID of the player who sent the request", example = "1", required = true)
            @PathVariable Long senderId,
            @Parameter(description = "Email of the player who received the request",
                    example = "recipient@example.com", required = true)
            @RequestParam String recipientEmail) {
        PlayerResponse approvedFriend = playerService.addFriend(senderId, recipientEmail);
        return ResponseEntity.ok(approvedFriend);
    }

    @Operation(
            summary = "Send bulk friend requests",
            description = "Sends multiple friend requests from one player to others by their emails"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend requests sent successfully"),
            @ApiResponse(responseCode = "404", description = "Sender or some recipients not found"),
            @ApiResponse(responseCode = "409", description = "Already friends with some recipients")
    })
    @PostMapping("/{senderId}/bulk-friend-requests")
    public ResponseEntity<List<PlayerResponse>> sendBulkFriendRequests(
            @Parameter(description = "ID of the sender player", example = "2", required = true)
            @PathVariable Long senderId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of recipient emails",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(
                                    value = "[\"gom7@gmail.com\", \"newemail@example.com\"]"
                            )
                    )
            )
            @RequestBody List<String> recipientEmails) {

        List<PlayerResponse> responses = playerService.processBulkFriendRequests(senderId, recipientEmails);
        return ResponseEntity.ok(responses);
    }
}