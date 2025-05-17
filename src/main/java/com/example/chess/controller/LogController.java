package com.example.chess.controller;

import com.example.chess.service.LogService;
import com.example.chess.service.LogTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Logs", description = "Application logs management endpoints")
@RestController
@RequestMapping("logs")
public class LogController {

    private final LogTaskService logTaskService;

    public LogController(LogTaskService logTaskService) {
        this.logTaskService = logTaskService;
    }

    @Operation(
            summary = "Generate log file asynchronously",
            description = "Initiates log file generation for the specified date and returns a task ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task ID generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping("/generate")
    public String generateLogFile(
            @Parameter(description = "Date in yyyy-MM-dd format", example = "2025-05-13",
                    required = true)
            @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}",
                    message = "Date must be in yyyy-MM-dd format") String date) {
        return logTaskService.generateLogFile(date);
    }

    @Operation(
            summary = "Get task status",
            description = "Retrieves the status of a log generation task by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task ID not found",
                    content = @Content)
    })
    @GetMapping("/status")
    public String getLogStatus(
            @Parameter(description = "Task ID", example = "123e4567-e89b-12d3-a456-426614174000",
                    required = true)
            @RequestParam String id) {
        return logTaskService.getStatus(id);
    }

    @Operation(
            summary = "Download generated log file",
            description = "Retrieves the generated log file content by task ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log file retrieved successfully",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "2025-05-13 09:05:45.123 [main] "
                                    + "INFO  com.example.MyClass - Sample log message"))),
            @ApiResponse(responseCode = "404", description = "Task or log file not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Error reading log file",
                    content = @Content)
    })
    @GetMapping("/download")
    public List<String> getLogFile(
            @Parameter(description = "Task ID", example = "123e4567-e89b-12d3-a456-426614174000",
                    required = true)
            @RequestParam String id) {
        return logTaskService.getLogs(id);
    }

    @Operation(
            summary = "Get logs by date",
            description = "Retrieves application logs for the specified date."
                    + " Returns current logs file for today's date "
                    + "or archived logs for previous dates."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs retrieved successfully",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "2025-05-13 09:05:45.123 [main] "
                                    + "INFO  com.example.MyClass - Sample log message"))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid date format or date exceeds retention period",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No logs found for the specified date",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error while processing log files",
                    content = @Content)
    })
    @GetMapping("/get")
    public byte[] getLog(
            @Parameter(description = "Date in yyyy-MM-dd format", example = "2025-05-13",
                    required = true)
            @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}",
                    message = "Date must be in yyyy-MM-dd format") String date) {
        return logTaskService.getLogsByDate(date);
    }

    @GetMapping("/allTasks")
    public List<String> getAllTasks() {
        return logTaskService.getAllTaskIds();
    }
}