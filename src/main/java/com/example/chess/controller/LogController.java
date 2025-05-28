package com.example.chess.controller;

import com.example.chess.exception.LogsException;
import com.example.chess.service.implementation.LogTaskServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Log Controller")
@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogTaskServiceImpl logServiceImpl;

    public LogController(LogTaskServiceImpl logTaskService) {
        this.logServiceImpl = logTaskService;
    }

    @PostMapping("/generate")
    public String generateLogFile(@RequestParam @Pattern(regexp =
            "\\d{4}-\\d{2}-\\d{2}", message = "Неправильный формат даты: yyyy-MM-dd") String  date)
            throws InterruptedException {
        return logServiceImpl.generateLogFile(date);
    }

    @GetMapping("/status")
    public String getLogStatus(@RequestParam String id) {
        return logServiceImpl.getStatus(id);
    }

    @GetMapping("/download")
    public List<String> getLogFile(@RequestParam String id) {
        try {
            return logServiceImpl.getLogs(id);
        } catch (Exception e) {
            throw new LogsException("Файл не найден");
        }
    }

}