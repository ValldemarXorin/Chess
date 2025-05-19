package com.example.chess.service;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VisitorCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Async("taskExecutor")
    public synchronized void increment() {
        counter.incrementAndGet();
    }

    @Async("taskExecutor")
    public synchronized CompletableFuture<String> getCounter() {
        return CompletableFuture
                .completedFuture(String.format("Количество посещений: %d", counter.get()));
    }

    @Async("taskExecutor")
    public synchronized CompletableFuture<String> reset() {
        counter.lazySet(0);
        if (counter.get() != 0) {
            throw new IllegalArgumentException("Ошибка сброса счетчика");
        }
        return CompletableFuture
                .completedFuture("Cчётчик успешно сброшен");
    }
}