package com.example.chess.service;

import java.util.List;

public interface LogTaskService {

    String generateLogFile(String date) throws InterruptedException;

    List<String> getLogs(String date);

    String getStatus(String id);
}
