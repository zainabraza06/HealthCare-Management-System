package com.example;

import java.util.List;


public class NotificationService {
    private final Notifiable emailNotifier;
    private final Notifiable whatsappNotifier;


    public NotificationService(Notifiable emailNotifier, Notifiable whatsappNotifier) {
        this.emailNotifier = emailNotifier;
        this.whatsappNotifier = whatsappNotifier;
    }

    // Emergency alert to multiple doctors
    public void sendEmergencyAlert(List<String> doctorEmails, List<String> doctorPhones,
                                 String patientId, String emergencyType) 
                                 throws NotificationException {
        String priority = emergencyType.equals("Code Blue") ? "IMMEDIATE" : "URGENT";
        
        try {
            // Batch send to all doctors
            for (int i = 0; i < doctorEmails.size(); i++) {
                String email = doctorEmails.get(i);
                String phone = formatPakistaniNumber(doctorPhones.get(i));
                
                // Email notification
                String emailMessage = String.format(
                    "Emergency Alert (%s)\n\n" +
                    "Patient ID: %s\n" +
                    "Emergency: %s\n" +
                    "Required: %s response\n\n" +
                    "Login to EHR: https://ehr.example.com/emergency/%s",
                    priority, patientId, emergencyType, priority, 
                    patientId);
                
                emailNotifier.sendNotification(email, emailMessage);
                
                // WhatsApp notification
                String whatsappMessage = String.format(
                    "🚨 *%s EMERGENCY*\n" +
                    "Patient: %s\n" +
                    "Type: %s\n\n" +
                    "Reply status:\n" +
                    "1 - Accepting case\n" +
                    "2 - Not available",
                    emergencyType, patientId, priority);
                
                whatsappNotifier.sendNotification(phone, whatsappMessage);
            }
        } catch (Exception e) {
            throw new NotificationException("Failed to send emergency alerts", e);
        }
    }

    // Critical lab results notification
    public void sendCriticalResults(String doctorEmail, String doctorPhone,
                                  String patientName, String testName, 
                                  String abnormalValue) throws NotificationException {
        try {
            // Email notification
            String emailMessage = String.format(
                "Critical Lab Results\n\n" +
                "Patient: %s\n" +
                "Test: %s\n" +
                "Abnormal Value: %s\n\n" +
                "Required Action: Review within 2 hours\n" +
                "Access full report: https://ehr.example.com/labs/%s",
                patientName, testName, abnormalValue, 
                patientName.toLowerCase().replace(" ", "-"));
            
            emailNotifier.sendNotification(doctorEmail, emailMessage);
            
            // WhatsApp notification
            String whatsappMessage = String.format(
                "⚠️ *Critical Results*\n" +
                "Patient: %s\n" +
                "%s: %s\n\n" +
                "Urgent review needed",
                patientName, testName, abnormalValue);
            
            whatsappNotifier.sendNotification(formatPakistaniNumber(doctorPhone), whatsappMessage);
        } catch (Exception e) {
            throw new NotificationException("Failed to send lab results", e);
        }
    }

    // Helper method to format Pakistani numbers
    private String formatPakistaniNumber(String rawNumber) {
        String digits = rawNumber.replaceAll("[^0-9]", "");
        if (digits.startsWith("92") && digits.length() == 11) {
            return "+" + digits;
        } else if (digits.startsWith("3") && digits.length() == 10) {
            return "+92" + digits;
        }
        return rawNumber; // Return as-is if already formatted
    }

    public void sendEmergencyAlert(List<String> recipientEmails,
                             List<String> recipientPhones,
                             String patientId,
                             String emergencyType,
                             String detailedMessage) throws NotificationException {
    
  
    // WhatsApp message (more concise)
    String whatsappMessage = String.format(
        "*%s*\nPatient ID: %s\n\n" +
        "Reply with:\n1-ACK\n2-ENROUTE\n3-CALLBACK",
        emergencyType, patientId
    );

    try {
        // Send to all email contacts
        for (String email : recipientEmails) {
            emailNotifier.sendNotification(email, detailedMessage);
        }
        
        // Send to all phone contacts
        for (String phone : recipientPhones) {
            whatsappNotifier.sendNotification(formatPakistaniNumber(phone), whatsappMessage);
        }
    } catch (Exception e) {
        throw new NotificationException("Failed to send panic alert", e);
    }
}
}