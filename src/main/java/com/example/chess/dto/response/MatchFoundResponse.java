package com.example.chess.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MatchFoundResponse {
    private long gameId;
    private String color;
}
