package com.example.chess.controller;

import com.example.chess.service.VisitorCounter;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("counter")
public class VisitorCounterController {

    private final VisitorCounter counter;

    public VisitorCounterController(VisitorCounter counter) {
        this.counter = counter;
    }

    @GetMapping
    public CompletableFuture<String> getCounter() {
        return counter.getCounter();
    }

    @PutMapping
    public CompletableFuture<String> reset() {
        return counter.reset();
    }
}