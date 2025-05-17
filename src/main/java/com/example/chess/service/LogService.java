package com.example.chess.service;

import com.example.chess.entity.LogTask;

public interface LogService {
    public void createLogFileByDate(String date, LogTask task);
}
