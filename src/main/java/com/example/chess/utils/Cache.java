package com.example.chess.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Cache<K,V> {
    private final Logger logger = LoggerFactory.getLogger(Cache.class);

    private final Map<K,Node> cache;
    private final int capacity;

    @Getter
    public class Node {

        @Setter
        private V value;

        private long frequency;

        public Node(V value) {
            this.value = value;
            this.frequency = 1;
        }

        public void incrementFrequency() {
            frequency++;
        }
    }

    public V getValue(K key) {
        Node node = cache.get(key);
        if (node == null) {
            return null;
        }
        node.incrementFrequency();
        return node.getValue();
    }

    public void putValue(K key, V value) throws IllegalStateException {
        if (cache.containsKey(key)) {
            cache.compute(key, (k, node) -> {
                if (node == null) {
                    return new Node(value);  // Если ключ не существует, создаем новый узел
                }
                node.incrementFrequency();  // Если существует, обновляем частоту
                node.setValue(value);  // И обновляем значение
                return node;
            });
            return;
        }
        
        if (isEvictionNeed()) {
            K keyToRemove = findKeyToRemove();
            if (keyToRemove == null) {
                new IllegalStateException("Eviction triggered but no key found to remove!");
            }
            remove(keyToRemove);
            logger.info(String.format("Removing %s", keyToRemove));
        }
        cache.put(key, new Node(value));
        logger.info(String.format("Putting %s to %s", key, value));
    }

    public boolean isEvictionNeed() {
        return cache.size() >= capacity;
    }

    public K findKeyToRemove() {
        K keyToRemove = null;
        long minFrequency = Long.MAX_VALUE;
        for (Map.Entry<K, Node> node : cache.entrySet()) {
            if (node.getValue().getFrequency() < minFrequency) {
                minFrequency = node.getValue().getFrequency();
                keyToRemove = node.getKey();
            }
        }
        return keyToRemove;
    }


    public void remove(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
            logger.info("Removed key: " + key);
        } else {
            logger.warn("Remove called with key " + key);
        }
    }

    public Cache(@Value("${cache.max-size:52428800}") int capacity) {
        this.capacity = capacity;
        cache = new ConcurrentHashMap<>();
        logger.info("Cache created with capacity: " + capacity);
    }
}
