package com.example.chess.service.implementation;

import com.example.chess.entity.LogTask;
import com.example.chess.exception.LogsException;
import com.example.chess.service.LogFileService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogFileServiceImpl implements LogFileService {

    @Override
    @Async("taskExecutor")
    public void creatLogFileByDate(String date, LogTask task) throws InterruptedException {
        task.setStatus("Processing");
        String failedTaskStatus = "Failed";

        Thread.sleep(15000);
        Path sourceLogPath = Path.of("logback", "chess.log");
        Path dateLogFile = Path.of("logback", String.format("chess-%s.log", date));

        task.setFilePath(dateLogFile.toString());

        if (!Files.exists(sourceLogPath)) {
            task.setStatus(failedTaskStatus);
            throw new LogsException("Исходный лог-файл не найден");
        }

        try (BufferedReader reader = Files.newBufferedReader(sourceLogPath);
             BufferedWriter writer = Files.newBufferedWriter(dateLogFile)) {
            String log;

            while ((log = reader.readLine()) != null) {
                if (log.contains(date)) {
                    writer.write(log + "\n");
                }
            }


        } catch (IOException e) {
            task.setStatus(failedTaskStatus);
            throw new LogsException("Ошибка при работе с лог-файлом");
        }

        task.setStatus("Completed");

    }
}