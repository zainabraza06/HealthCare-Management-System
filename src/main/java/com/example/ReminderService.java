package com.example;



public class ReminderService {
    private final EmailNotification emailService;
    private final WhatsAppNotification whatsappService;
    
    public ReminderService(EmailNotification emailService, WhatsAppNotification whatsappService) {
        this.emailService = emailService;
        this.whatsappService = whatsappService;
    }

  
    public void sendAppointmentReminder(Appointment appointment, Patient patient) {
        String message = String.format(
            "Reminder: You have an appointment with Dr. %s on %s at %s",
            appointment.getDoctorId(),
            appointment.getDateTime().toLocalDate(),
            appointment.getDateTime().toLocalTime()
        );
    
        sendToBothChannels(patient.getContactInfo(), message);
    }
    
    public void sendStatusNotification(Appointment appointment, Appointment.Status status, Patient patient) {
        String message = String.format(
            "Appointment Update: Your appointment on %s is now %s",
            appointment.getDateTime(),
            status.toString()
        );
    
        if (status == Appointment.Status.CANCELLED || status == Appointment.Status.NO_SHOW) {
            message += ". Reason: " + appointment.getCancellationReason();
        }
    
        sendToBothChannels(patient.getContactInfo(), message);
    }
    

   
    public void sendMedicationReminder(Prescription prescription, Patient patient) {
        String message = String.format(
            "Medication Reminder: Take %s %s as prescribed by Dr. %s",
            prescription.getMedication().getName(),
            prescription.getDosage().toString(),
            prescription.getPrescribingDoctor().getFullName()
        );
        
        sendToBothChannels(patient.getContactInfo(), message);
    }

  

    private void sendToBothChannels(User.ContactInfo contact, String message) {
        try {
            emailService.sendNotification(contact.getEmail(), message);
            whatsappService.sendNotification(contact.getPhone(), message);
        } catch (NotificationException e) {
            System.err.println("Failed to send reminder: " + e.getMessage());
        }
    }
}