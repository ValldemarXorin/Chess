package com.example.chess.service;

import com.example.chess.exception.ValidationException;

import java.io.IOException;
import java.time.LocalDate;

public interface LogService {

    public byte[] getLogsByDate(String date);

}
