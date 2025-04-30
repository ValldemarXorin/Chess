package com.example.chess.testService;

import com.example.chess.exception.LogsException;
import com.example.chess.exception.ResourceNotFoundException;
import com.example.chess.exception.ValidationException;
import com.example.chess.service.implementation.LogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @InjectMocks
    private LogServiceImpl logService;

    @Test
    void getLogsByDate_CurrentDate_ReturnsLogs() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Arrange
            String today = LocalDate.now().toString();
            byte[] expectedLogs = "test logs".getBytes();

            filesMock.when(() -> Files.exists(Path.of("logback/chess.log"))).thenReturn(true);
            filesMock.when(() -> Files.readAllBytes(Path.of("logback/chess.log"))).thenReturn(expectedLogs);

            // Act
            byte[] result = logService.getLogsByDate(today);

            // Assert
            assertArrayEquals(expectedLogs, result);
        }
    }

    @Test
    void getLogsByDate_ArchivedDate_ReturnsLogs() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Arrange
            String pastDate = LocalDate.now().minusDays(1).toString();
            byte[] logContent = "archived logs".getBytes();
            byte[] expectedLogs = "archived logs\n".getBytes(); // Ожидаем с переводом строки

            Path mockArchivePath = Path.of("logback/archived/chess." + pastDate + ".1.log");

            filesMock.when(() -> Files.list(Path.of("logback/archived")))
                    .thenReturn(Stream.of(mockArchivePath));
            filesMock.when(() -> Files.readAllBytes(mockArchivePath))
                    .thenReturn(logContent);

            // Act
            byte[] result = logService.getLogsByDate(pastDate);

            // Assert
            assertArrayEquals(expectedLogs, result,
                    "Returned logs should match expected content with newline");
        }
    }

    @Test
    void getLogsByDate_InvalidDateFormat_ThrowsValidationException() {
        // Arrange
        String invalidDate = "2024/01/01";

        // Act & Assert
        assertThrows(ValidationException.class, () -> logService.getLogsByDate(invalidDate));
    }

    @Test
    void getLogsByDate_DateExceedsRetention_ThrowsValidationException() {
        // Arrange
        String oldDate = LocalDate.now().minusDays(8).toString();

        // Act & Assert
        assertThrows(ValidationException.class, () -> logService.getLogsByDate(oldDate));
    }

    @Test
    void getLogsByDate_CurrentDateNoLogs_ThrowsResourceNotFoundException() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Arrange
            String today = LocalDate.now().toString();

            filesMock.when(() -> Files.exists(Path.of("logback/chess.log"))).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> logService.getLogsByDate(today));
        }
    }

    @Test
    void getLogsByDate_ArchivedDateNoLogs_ThrowsResourceNotFoundException() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Arrange
            String pastDate = LocalDate.now().minusDays(1).toString();

            filesMock.when(() -> Files.list(Path.of("logback/archived"))).thenReturn(Stream.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> logService.getLogsByDate(pastDate));
        }
    }

    @Test
    void getLogsByDate_IOException_ThrowsLogsException() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Arrange
            String today = LocalDate.now().toString();

            filesMock.when(() -> Files.exists(Path.of("logback/chess.log"))).thenReturn(true);
            filesMock.when(() -> Files.readAllBytes(Path.of("logback/chess.log")))
                    .thenThrow(new IOException("File read error"));

            // Act & Assert
            assertThrows(LogsException.class, () -> logService.getLogsByDate(today));
        }
    }
}