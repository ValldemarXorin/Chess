package com.example.chess.service.implementation;

import com.example.chess.entity.LogTask;
import com.example.chess.exception.LogsException;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.exception.ValidationException;
import com.example.chess.service.LogService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    @Override
    @Async("taskExecutor")
    public void createLogFileByDate(String date, LogTask task) {
        task.setStatus("Processing");
        String failedTaskStatus = "Failed";

        Path sourceLogPath = Path.of("logback", "chess.log");
        Path dateLogFile = Path.of("logback/archived", String.format("chess-%s.log", date));

        // Check for file conflict
        if (Files.exists(dateLogFile)) {
            task.setStatus(failedTaskStatus);
            throw new LogsException("Log file for date " + date + " already exists");
        }

        task.setFilePath(dateLogFile.toString());

        if (!Files.exists(sourceLogPath)) {
            task.setStatus(failedTaskStatus);
            throw new LogsException("Source log file not found");
        }

        try (BufferedReader reader = Files.newBufferedReader(sourceLogPath);
             BufferedWriter writer = Files.newBufferedWriter(dateLogFile)) {
            String log;
            while ((log = reader.readLine()) != null) {
                // Проверяем, что строка начинается с нужной даты
                if (log.startsWith(date + " ")) {
                    writer.write(log + "\n");
                }
            }
        } catch (Exception e) {
            task.setStatus(failedTaskStatus);
            throw new LogsException("Error processing log file: " + e.getMessage());
        }

        task.setStatus("Completed");
    }
}
