package com.example.chess.controller;

import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.exception.LogsException;
import com.example.chess.exception.ValidationException;
import com.example.chess.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tag(name = "Logs", description = "Application logs management endpoints")
@RestController
@RequestMapping("logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(
            summary = "Get logs by date",
            description = "Retrieves application logs for the specified date. Returns current logs file for today's date "
                    + "or archived logs for previous dates."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logs retrieved successfully",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "2024-01-15 10:30:45.123 [main] INFO  com.example.MyClass - Sample log message"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date format or date exceeds retention period",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No logs found for the specified date",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while processing log files",
                    content = @Content
            )
    })
    @GetMapping("/get")
    public byte[] getLog(
            @Parameter(
                    description = "Date in yyyy-MM-dd format",
                    example = "2024-01-15",
                    required = true
            )
            @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}",
                    message = "Date must be in yyyy-MM-dd format") String date) {

        return logService.getLogsByDate(date);
    }
}