package com.example.chess.exception;

import com.example.chess.dto.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

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
        return buildResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST,
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
