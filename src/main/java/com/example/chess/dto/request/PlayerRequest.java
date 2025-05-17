package com.example.chess.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerRequest {
    private String name;
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Некорректный email")
    private String email;
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=(.*[!%?#$_]))[A-Za-z0-9!%?#$_]{8,30}$",
            message = "Пароль должен содержать: 8-30 символов, латинские буквы (A-Z, a-z),"
                    + " цифры (0-9), спецсимволы (!%%?#$_), минимум 1 заглавную букву, 1 строчную,"
                    + " 1 цифру и 1 спецсимвол."
    )
    private String password;

    public PlayerRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
