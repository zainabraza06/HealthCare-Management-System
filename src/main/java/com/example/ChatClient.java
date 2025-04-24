package com.example;

import java.util.*;
import java.util.concurrent.*;

public class ChatClient implements Runnable {
    private final String username;
    private final ChatServer server;
    private final BlockingQueue<String> incomingMessages = new LinkedBlockingQueue<>();
    private volatile boolean connected = true;

    public ChatClient(String username, ChatServer server) {
        this.username = username;
        this.server = server;
    }

    @Override
    public void run() {
        System.out.printf("Chat Client '%s' started (type '/exit' to quit)%n", username);
        
        // Start message receiver thread
        new Thread(this::receiveMessages).start();
        
        // Handle user input
        try (Scanner scanner = new Scanner(System.in)) {
            while (connected) {
                String input = scanner.nextLine();
                
                if ("/exit".equalsIgnoreCase(input)) {
                    disconnect();
                    break;
                }
                
                server.sendMessage(username, input);
            }
        }
    }

    private void receiveMessages() {
        while (connected) {
            try {
                String message = incomingMessages.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    System.out.println("[Server]: " + message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendMessage(String sender, String message) {
        incomingMessages.add(String.format("[%s]: %s", sender, message));
    }

    public void disconnect() {
        connected = false;
        System.out.println("Disconnected from server");
    }

}