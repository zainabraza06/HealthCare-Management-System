package com.example;

public interface Notifiable {
    void sendNotification(String recipient, String message) throws NotificationException;
}



 class NotificationException extends Exception {


    public NotificationException(String message) {
        super(message);
    }
    

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}