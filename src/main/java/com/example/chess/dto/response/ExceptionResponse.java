package com.example.chess.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ExceptionResponse {
    private String message;
    private HttpStatus httpStatus;
    private LocalDateTime timestamp;
    private String path;
}
