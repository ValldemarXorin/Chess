package com.example.chess.service;

import java.util.List;

public interface LogTaskService {
    String generateLogFile(String date);
    String getStatus(String taskId);
    List<String> getLogs(String taskId);
    byte[] getLogsByDate(String date);
    public List<String> getAllTaskIds();
}
