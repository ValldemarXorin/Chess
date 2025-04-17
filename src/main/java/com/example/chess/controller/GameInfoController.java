package com.example.chess.controller;

import com.example.chess.dto.request.GameInfoRequest;
import com.example.chess.dto.response.GameInfoResponse;
import com.example.chess.service.GameInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Game Management", description = "Endpoints for managing chess games information")
@RestController
@RequestMapping("/games")
public class GameInfoController {
    private final GameInfoService gameInfoService;

    public GameInfoController(GameInfoService gameInfoService) {
        this.gameInfoService = gameInfoService;
    }

    @Operation(
            summary = "Create new game",
            description = "Registers a new chess game in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Game created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GameInfoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid game data provided"
            )
    })
    @PostMapping
    public ResponseEntity<GameInfoResponse> createGame(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Game information to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GameInfoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                            "player1Id": 1,
                            "player2Id": 2,
                            "winnerId": 1,
                            "startTime": "2023-05-15T14:30:00",
                            "endTime": "2023-05-15T15:45:00",
                            "moves": "e4 e5 Nf3 Nc6 Bb5 a6"
                        }
                        """
                            )
                    )
            )
            @RequestBody GameInfoRequest gameInfoDto) {
        return ResponseEntity.ok(gameInfoService.createGame(gameInfoDto));
    }

    @Operation(
            summary = "Get game by ID",
            description = "Retrieves detailed information about a specific chess game"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Game found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GameInfoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<GameInfoResponse> getGameById(
            @Parameter(
                    description = "ID of the game to retrieve",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {
        return ResponseEntity.ok(gameInfoService.getGameById(id));
    }

    @Operation(
            summary = "Get all games",
            description = "Retrieves information about all chess games in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Games retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GameInfoResponse[].class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<GameInfoResponse>> getAllGames() {
        return ResponseEntity.ok(gameInfoService.getAllGames());
    }

    @Operation(
            summary = "Update game",
            description = "Updates information about a specific chess game"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Game updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GameInfoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid game data provided"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<GameInfoResponse> updateGame(
            @Parameter(
                    description = "ID of the game to update",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated game information",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GameInfoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                            "player1Id": 1,
                            "player2Id": 2,
                            "winnerId": 2,
                            "startTime": "2023-05-15T14:30:00",
                            "endTime": "2023-05-15T15:45:00",
                            "moves": "e4 e5 Nf3 Nc6 Bb5 a6 Ba4 Nf6 O-O Be7 Re1 b5"
                        }
                        """
                            )
                    )
            )
            @RequestBody GameInfoRequest gameInfoDto) {
        return ResponseEntity.ok(gameInfoService.updateGame(id, gameInfoDto));
    }

    @Operation(
            summary = "Delete game",
            description = "Removes a chess game from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Game deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(
            @Parameter(
                    description = "ID of the game to delete",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {
        gameInfoService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}