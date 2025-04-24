package com.example;

import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private final Map<String, List<String>> messageQueues = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private volatile boolean running = true;

    public void start() {
        System.out.println("Chat Server started (type 'shutdown' to stop)");
        executor.submit(this::processMessages);
        
        // Simulate server console input
        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                if (scanner.nextLine().equalsIgnoreCase("shutdown")) {
                    shutdown();
                }
            }
        }
    }

    private void processMessages() {
        while (running) {
            try {
                // Process all message queues
                for (Map.Entry<String, List<String>> entry : messageQueues.entrySet()) {
                    String user = entry.getKey();
                    List<String> messages = entry.getValue();
                    
                    if (!messages.isEmpty()) {
                        String msg = messages.remove(0);
                        System.out.printf("[%s received]: %s%n", user, msg);
                        
                        // Echo back to sender
                        sendMessage(user, "ECHO: " + msg);
                    }
                }
                Thread.sleep(100); // Small delay to prevent CPU overload
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendMessage(String recipient, String message) {
        messageQueues.computeIfAbsent(recipient, k -> new CopyOnWriteArrayList<>())
                    .add(message);
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
        System.out.println("Chat Server stopped");
    }

  
}