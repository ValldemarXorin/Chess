package com.example.chess.exception;

import com.example.chess.dto.response.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    private ResponseEntity<ExceptionResponse> buildResponseEntity(String message,
                                                                  HttpStatus httpStatus,
                                                                  String path) {
        return new ResponseEntity<>(new ExceptionResponse(message, httpStatus,
                                    LocalDateTime.now(), path), httpStatus);
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(
                                                                    ResourceNotFoundException e,
                                                                    WebRequest request) {
        return buildResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND,
                request.getDescription(false));
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequestException(
                                                                    ConflictException e,
                                                                    WebRequest request) {
        return buildResponseEntity(e.getMessage(), HttpStatus.CONFLICT,
                request.getDescription(false));
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
                                                                    MethodArgumentNotValidException e,
                                                                    WebRequest request) {
        log.error(e.getMessage());
        return buildResponseEntity("Not valid email or password. Email template: example@example.com" +
                        " Password: only latin letters, numbers, specific symbols (min required 1 specific" +
                        " symbol, 1 letter for each register, 1 number, more than 8 characters," +
                        " less then 30 charachters", HttpStatus.BAD_REQUEST,
                request.getDescription(false));
    }

    @ExceptionHandler(value = {LogsException.class})
    public ResponseEntity<ExceptionResponse> handleLogsException(
                                                                    LogsException e,
                                                                    WebRequest request) {
        return buildResponseEntity(e.getMessage(), HttpStatus.CONFLICT,
                request.getDescription(false));
    }
}
