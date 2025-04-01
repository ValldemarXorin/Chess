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

    private final Map<K, Node> mainCache;
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
        if (!mainCache.containsKey(key)) {
            return null;
        }
        Node node = mainCache.get(key);
        updateFrequency(key, node);
        return node.value;
    }

    public void putValue(K key, V value) throws IllegalStateException {

        Node existingNode = mainCache.computeIfPresent(key, (k, node) -> {
            node.setValue(value);
            updateFrequency(key, node);
            logger.info("Successful updating data for key: {} with value: {}", key, value);
            return node;
        });

        if (existingNode != null) {
            logger.info("Node with key {} already exists", key);
            return;
        }
        
        if (isEvictionNeed()) {
            evict();
            logger.info("Successful evict");
        }

        mainCache.put(key, new Node(value));
        frequencyMap.computeIfAbsent(1L, k -> new LinkedHashSet<>()).add(key);
        minFrequency = 1;
        logger.info("Putting {} to {}", key, value);
    }

    public boolean isEvictionNeed() {
        return mainCache.size() >= capacity;
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
        mainCache.remove(keyToRemove);
        logger.info("Removing {}", keyToRemove);
    }


    public void remove(K key) {
        if (!mainCache.containsKey(key)) {
            logger.warn("Key {} doesnt exist ", key);
        }

        Node node = mainCache.remove(key);
        long freq = node.getFrequency();

        LinkedHashSet<K> keys = frequencyMap.get(freq);
        keys.remove(key);
        if (keys.isEmpty()) {
            frequencyMap.remove(freq);
        }
        logger.info("Removed key: {}", key);
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
        logger.info("Updating frequency {} to {}", key, node.getFrequency());
    }

    public void clear() {
        mainCache.clear();
        logger.info("Cache cleared.");
    }

    public Cache(@Value("${cache.max-size:52428800}") int capacity) {
        this.capacity = capacity;
        this.mainCache = new ConcurrentHashMap<>();
        this.frequencyMap = new ConcurrentHashMap<>();
        this.logger.info("Cache created with capacity: {}", capacity);
        this.minFrequency = 1;
    }
}
