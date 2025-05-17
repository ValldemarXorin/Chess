package com.example.chess.service.implementation;

import com.example.chess.entity.LogTask;
import com.example.chess.exception.LogsException;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.exception.ValidationException;
import com.example.chess.service.LogService;
import com.example.chess.service.LogTaskService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LogTaskServiceImpl implements LogTaskService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int MAX_LOG_DAYS = 7;

    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();
    private final LogService logFileService;

    public LogTaskServiceImpl(LogService logFileService) {
        this.logFileService = logFileService;
    }

    @Override
    public String generateLogFile(String date) {
        try {
            LocalDate.parse(date, INPUT_DATE_FORMAT); // Validate date format
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use yyyy-MM-dd");
        }

        LogTask task = new LogTask();
        String taskId = UUID.randomUUID().toString();
        task.setId(taskId);
        tasks.put(taskId, task);

        logFileService.createLogFileByDate(date, task);
        task.setStatus("In proccessing");

        return taskId;
    }

    @Override
    @Transactional
    public String getStatus(String taskId) {
        LogTask task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " not found");
        }
        return task.getStatus();
    }

    @Override
    @Transactional
    public List<String> getLogs(String taskId) {
        LogTask task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " not found");
        }

        if (!task.getStatus().equals("Completed")) {
            throw new LogsException("Log file for task " + taskId + " is not ready");
        }

        Path dateLogFile = Path.of(task.getFilePath());
        if (!Files.exists(dateLogFile)) {
            throw new LogsException("Log file for task " + taskId + " not found");
        }

        try {
            return Files.readAllLines(dateLogFile);
        } catch (IOException e) {
            throw new LogsException("Error reading log file: " + e.getMessage());
        }
    }

    @Override
    public byte[] getLogsByDate(String date) {
        try {
            LocalDate logDate = LocalDate.parse(date, INPUT_DATE_FORMAT);
            LocalDate today = LocalDate.now();

            validateDate(logDate, today);

            if (logDate.equals(today)) {
                return getCurrentLogs();
            } else {
                return getArchivedLogs(date, logDate);
            }

        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use yyyy-MM-dd");
        } catch (IOException e) {
            throw new LogsException("Error processing log files: " + e.getMessage());
        }
    }

    private void validateDate(LocalDate logDate, LocalDate today) {
        if (logDate.isBefore(today.minusDays(MAX_LOG_DAYS))) {
            throw new ValidationException(String.format(
                    "No logs available for date %s (logs are kept for maximum %d days)",
                    logDate.format(OUTPUT_DATE_FORMAT), MAX_LOG_DAYS
            ));
        }
    }

    private byte[] getCurrentLogs() throws IOException {
        Path currentLogPath = Path.of("logback/chess.log");
        if (!Files.exists(currentLogPath)) {
            throw new ResourceNotFoundException("No logs available for today yet");
        }
        return Files.readAllBytes(currentLogPath);
    }

    private byte[] getArchivedLogs(String date, LocalDate logDate) throws IOException {
        List<Path> archiveFiles;
        try (Stream<Path> paths = Files.list(Path.of("logback/archived"))) {
            archiveFiles = paths
                    .filter(path -> path.getFileName().toString()
                            .matches("chess\\." + date + "\\.\\d+\\.log"))
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

    @Override
    public List<String> getAllTaskIds() {
        return new ArrayList<>(tasks.keySet());
    }
}
