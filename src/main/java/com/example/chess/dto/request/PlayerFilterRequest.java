package com.example.chess.dto.request;

import lombok.Data;

@Data
public class PlayerFilterRequest {
    private String status;
    private String notes;
    private Integer page = 0;
    private Integer size = 10;
}