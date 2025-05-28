package com.example.chess.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerUpdateRequest {
    private String name;

    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Некорректный email")
    private String email;

    public PlayerUpdateRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }
}