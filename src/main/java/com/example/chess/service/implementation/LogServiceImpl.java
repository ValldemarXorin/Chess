package com.example.chess.service.implementation;

import com.example.chess.service.LogService;
import org.springframework.stereotype.Service;

import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.exception.LogsException;
import com.example.chess.exception.ValidationException;
import org.springframework.stereotype.Service;

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

@Service
public class LogServiceImpl implements LogService {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int MAX_LOG_DAYS = 7;

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

}
