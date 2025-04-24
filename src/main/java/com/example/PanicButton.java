package com.example;

import java.time.LocalDateTime;

public class PanicButton {
    private final Patient patient;
    private final Notifiable whatsappNotifier;
    private final Notifiable emailNotifier;

    private boolean isActive = false;
    private LocalDateTime lastActivated;

    public PanicButton(Patient patient, Notifiable whatsappNotifier, Notifiable emailNotifier) {
        this.patient = patient;
        this.whatsappNotifier = whatsappNotifier;
        this.emailNotifier = emailNotifier;
    }

    public synchronized void activate() throws NotificationException {
        if (isActive) {
            throw new IllegalStateException("Panic button already active");
        }
        if (isOnCooldown()) {
            throw new IllegalStateException("Panic button is on cooldown");
        }

        isActive = true;
        lastActivated = LocalDateTime.now();

        String fullName = patient.getFullName();
        String location = patient.getCurrentLocation();
        User.EmergencyContact contact = patient.getEmergencyContact();
        String message = String.format(
            "🚨 EMERGENCY ALERT 🚨\nPatient: %s\nLocation: %s\nTime: %s",
            fullName, location, lastActivated
        );

        // Send WhatsApp
        whatsappNotifier.sendNotification(contact.getPhoneNumber(), message);

        // Send Email
        emailNotifier.sendNotification(contact.getEmail(), message);

        System.out.println("Panic button activated! Alerts sent to emergency contact.");
        isActive = false; // reset after sending
    }

    private boolean isOnCooldown() {
        return lastActivated != null &&
               lastActivated.plusMinutes(2).isAfter(LocalDateTime.now());
    }
}
