package com.example.chess.utils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Cache<K, V> {
    private final Logger logger = LoggerFactory.getLogger(Cache.class);

    private final Map<K, Node> cache;
    private final Map<Long, LinkedHashSet<K>> frequencyMap;
    private final int capacity;
    private long minFrequency;

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
        if (!cache.containsKey(key)) {
            return null;
        }
        Node node = cache.get(key);
        updateFrequency(key, node);
        return node.value;
    }

    public void putValue(K key, V value) throws IllegalStateException {

        Node existingNode = cache.computeIfPresent(key, (k, node) -> {
            node.setValue(value);
            updateFrequency(key, node);
            logger.info("Successful updating data for key: " + key + " with value: " + value);
            return node;
        });

        if (existingNode != null) {
            logger.info(String.format("Node with key %s already exists", key));
            return;
        }
        
        if (isEvictionNeed()) {
            evict();
            logger.info(String.format("Successful evict"));
        }

        cache.put(key, new Node(value));
        frequencyMap.computeIfAbsent(1L, k -> new LinkedHashSet<>()).add(key);
        minFrequency = 1;
        logger.info(String.format("Putting %s to %s", key, value));
    }

    public boolean isEvictionNeed() {
        return cache.size() >= capacity;
    }

    public void evict() {
        if (!frequencyMap.containsKey(minFrequency)) {
            return;
        }

        K keyToRemove = frequencyMap.get(minFrequency).iterator().next();
        frequencyMap.get(minFrequency).remove(keyToRemove);
        if (frequencyMap.get(minFrequency).isEmpty()) {
            frequencyMap.remove(minFrequency);
        }
        cache.remove(keyToRemove);
        logger.info(String.format("Removing %s", keyToRemove));
    }


    public void remove(K key) {
        if (!cache.containsKey(key)) {
            logger.warn(String.format("Key %s doesnt exist ", key));
        }

        Node node = cache.remove(key);
        long freq = node.getFrequency();

        LinkedHashSet<K> keys = frequencyMap.get(freq);
        keys.remove(key);
        if (keys.isEmpty()) {
            frequencyMap.remove(freq);
        }
        logger.info("Removed key: " + key);
    }

    private void updateFrequency(K key, Node node) {
        long nodeOldFrequency = node.getFrequency();
        node.incrementFrequency();

        frequencyMap.get(nodeOldFrequency).remove(key);

        if (frequencyMap.get(nodeOldFrequency).isEmpty()) {
            frequencyMap.remove(nodeOldFrequency);
            if (minFrequency == nodeOldFrequency) {
                minFrequency = node.getFrequency();
            }
        }

        frequencyMap.computeIfAbsent(node.getFrequency(), k -> new LinkedHashSet<>()).add(key);
        logger.info(String.format("Updating frequency %s to %s", key, node.getFrequency()));
    }

    public void clear() {
        cache.clear();
        logger.info("Cache cleared.");
    }

    public Cache(@Value("${cache.max-size:52428800}") int capacity) {
        this.capacity = capacity;
        this.cache = new ConcurrentHashMap<>();
        this.frequencyMap = new ConcurrentHashMap<>();
        this.logger.info("Cache created with capacity: " + capacity);
        this.minFrequency = 1;
    }
}
