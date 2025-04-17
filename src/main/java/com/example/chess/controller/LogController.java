package com.example.chess.controller;

import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.exception.LogsException;
import com.example.chess.exception.ValidationException;
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

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int MAX_LOG_DAYS = 7;

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

        try {
            LocalDate logDate = LocalDate.parse(date, INPUT_DATE_FORMAT);
            LocalDate today = LocalDate.now();

            // Check date validity
            if (logDate.isBefore(today.minusDays(MAX_LOG_DAYS))) {
                throw new ValidationException(String.format(
                        "No logs available for date %s (logs are kept for maximum %d days)",
                        logDate.format(OUTPUT_DATE_FORMAT), MAX_LOG_DAYS
                ));
            }

            // For today's date - read current log file
            if (logDate.equals(today)) {
                Path currentLogPath = Path.of("logback/chess.log");
                if (!Files.exists(currentLogPath)) {
                    throw new ResourceNotFoundException("No logs available for today yet");
                }
                return Files.readAllBytes(currentLogPath);
            }
            // For previous dates - search archived files
            else {
                List<Path> archiveFiles;
                try (Stream<Path> paths = Files.list(Path.of("logback/archived"))) {
                    archiveFiles = paths
                            .filter(path -> path.getFileName().toString().matches("chess\\." + date + "\\.\\d+\\.log"))
                            .sorted()
                            .collect(Collectors.toList());
                }

                if (archiveFiles.isEmpty()) {
                    throw new ResourceNotFoundException(String.format("No logs found for date %s",
                            logDate.format(OUTPUT_DATE_FORMAT)));
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                for (Path archiveFile : archiveFiles) {
                    byte[] fileContent = Files.readAllBytes(archiveFile);
                    outputStream.write(fileContent);
                    outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                }

                return outputStream.toByteArray();
            }

        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use yyyy-MM-dd");
        } catch (IOException e) {
            throw new LogsException("Error processing log files: " + e.getMessage());
        }
    }
}