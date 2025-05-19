package com.example.chess.service.implementation;

import com.example.chess.entity.LogTask;
import com.example.chess.exception.LogsException;
import com.example.chess.service.LogTaskService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LogTaskServiceImpl implements LogTaskService {

    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();
    private final LogFileServiceImpl logFileServiceImpl;

    public LogTaskServiceImpl(LogFileServiceImpl logFileServiceImpl) {
        this.logFileServiceImpl = logFileServiceImpl;
    }

    @Override
    public String generateLogFile(String date) throws InterruptedException {
        LogTask task = new LogTask();
        String taskId = UUID.randomUUID().toString();
        task.setId(taskId);

        logFileServiceImpl.creatLogFileByDate(date, task);

        tasks.put(taskId, task);

        return taskId;

    }

    @Override
    @Transactional
    public String getStatus(String taskId) {
        LogTask task = tasks.get(taskId);
        if (task == null) {
            throw new LogsException("Задача с ID " + taskId + " не найдена");
        }
        return tasks.get(taskId).getStatus();
    }

    @Override
    @Transactional
    public List<String> getLogs(String taskId) {

        LogTask task = tasks.get(taskId);
        if (task == null) {
            throw new LogsException("Задача с ID " + taskId + " не найдена");
        }

        String filePath = task.getFilePath();
        if (filePath == null) {
            throw new LogsException("Файл не найден");
        }

        Path dateLogFile = Path.of(filePath);
        if (!Files.exists(dateLogFile)) {
            throw new LogsException("Файл не найден");
        }

        List<String> logs;
        try {
            logs = Files.readAllLines(dateLogFile);
        } catch (IOException e) {
            throw new LogsException("Ошибка при чтении лог-файла по пути "
                    + filePath + ": " + e.getMessage());
        }

        return logs;
    }
}
