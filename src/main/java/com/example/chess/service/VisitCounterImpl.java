package com.example.chess.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VisitCounterImpl {
    private final AtomicInteger counter = new AtomicInteger(0);

    public synchronized void increment() {
        counter.incrementAndGet();
    }

    @Async("taskExecutor")
    public synchronized CompletableFuture<String> getCounter() {
        return CompletableFuture
                .completedFuture(String.format("Количество посещений: %d", counter.get()));
    }
}