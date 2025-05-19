package com.example.chess.service;

import com.example.chess.entity.LogTask;

public interface LogFileService {
    void creatLogFileByDate(String date, LogTask task)  throws InterruptedException;
}
