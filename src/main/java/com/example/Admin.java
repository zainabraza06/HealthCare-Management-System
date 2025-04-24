package com.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;


public class Admin extends User {
    private List<Doctor> managedDoctors;
    private List<Patient> managedPatients;
    private List<SystemLog> systemLogs;

    public Admin(String userId, String firstName, String lastName, String email, 
                        String password, LocalDate dateOfBirth, String address, 
                        String phoneNumber, Gender gender, String nationality, 
                        User.EmergencyContact emergencyContact, BloodType bloodType, 
                        String identificationNumber, String preferredLanguage, 
                        String profileImageUrl, FeedbackManager feedbackManager) {
        
        super(userId, firstName, lastName, email, Role.ADMIN, password, dateOfBirth, 
              address, phoneNumber, gender, nationality, emergencyContact, bloodType, 
              identificationNumber, preferredLanguage, profileImageUrl, feedbackManager);
        
        this.managedDoctors = new ArrayList<>();
        this.managedPatients = new ArrayList<>();
        this.systemLogs = new ArrayList<>();
    }

    //  DOCTOR MANAGEMENT 
    public void addDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor cannot be null");
        }
        managedDoctors.add(doctor);
        logAction("Added doctor: " + doctor.getUserId() + " - " + doctor.getFullName());
    }

    public void removeDoctor(String doctorId) {
        managedDoctors.removeIf(d -> d.getUserId() == doctorId);
        logAction("Removed doctor with ID: " + doctorId);
    }

    public List<Doctor> getAllDoctors() {
        return new ArrayList<>(managedDoctors); // Return a copy for encapsulation
    }

    public Doctor findDoctorById(String doctorId) {
        return managedDoctors.stream()
                .filter(d -> d.getUserId() == doctorId)
                .findFirst()
                .orElse(null);
    }

    // ================= PATIENT MANAGEMENT =================
    public void addPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        managedPatients.add(patient);
        logAction("Added patient: " + patient.getUserId() + " - " + patient.getFullName());
    }

    public void removePatient(String patientId) {
        managedPatients.removeIf(p -> p.getUserId() == patientId);
        logAction("Removed patient with ID: " + patientId);
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(managedPatients); // Return a copy for encapsulation
    }

    public Patient findPatientById(String patientId) {
        return managedPatients.stream()
                .filter(p -> p.getUserId() == patientId)
                .findFirst()
                .orElse(null);
    }

    // ================= LOG MANAGEMENT =================
    private void logAction(String actionDescription) {
        SystemLog log = new SystemLog(
            LocalDateTime.now(),
           (this.getUserId()),
            actionDescription,
            SystemLog.Severity.INFO
        );
        systemLogs.add(log);
    }

    public void logSecurityEvent(String eventDescription, SystemLog.Severity severity) {
        SystemLog log = new SystemLog(
            LocalDateTime.now(),
            this.getUserId(),
            eventDescription,
            severity
        );
        systemLogs.add(log);
    }

    public List<SystemLog> getLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        List<SystemLog> result = new ArrayList<>();
        for (SystemLog log : systemLogs) {
            if (!log.getTimestamp().isBefore(start) && !log.getTimestamp().isAfter(end)) {
                result.add(log);
            }
        }
        return result;
    }

    public List<SystemLog> getLogsBySeverity(SystemLog.Severity severity) {
        List<SystemLog> result = new ArrayList<>();
        for (SystemLog log : systemLogs) {
            if (log.getSeverity() == severity) {
                result.add(log);
            }
        }
        return result;
    }

    public void clearOldLogs(LocalDateTime cutoffDate) {
        systemLogs.removeIf(log -> log.getTimestamp().isBefore(cutoffDate));
    }

    // ================= OVERRIDDEN METHODS =================
    @Override
    public void displayInfo() {
        System.out.println("Administrator Information:");
        System.out.println("ID: " + getUserId());
        System.out.println("Name: " + getFullName());
        System.out.println("Email: " + getEmail());
        System.out.println("Managed Doctors: " + managedDoctors.size());
        System.out.println("Managed Patients: " + managedPatients.size());
    }

    @Override
    public String toString() {
        return String.format(
            "Administrator[ID=%d, Name='%s', Email='%s', ManagedDoctors=%d, ManagedPatients=%d]",
            getUserId(), getFullName(), getEmail(), managedDoctors.size(), managedPatients.size()
        );
    }

    /**
     * Inner class representing a system log entry.
     */
    public static class SystemLog {
        private final LocalDateTime timestamp;
        private final String actorId;
        private final String action;
        private final Severity severity;

        public enum Severity { INFO, WARNING, ERROR, CRITICAL }

        public SystemLog(LocalDateTime timestamp, String actorId, String action, Severity severity) {
            this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
            this.actorId = actorId;
            this.action = Objects.requireNonNull(action, "Action cannot be null");
            this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getActorId() { return actorId; }
        public String getAction() { return action; }
        public Severity getSeverity() { return severity; }

        @Override
        public String toString() {
            return String.format(
                "SystemLog[timestamp=%s, actorId=%d, severity=%s, action='%s']",
                timestamp, actorId, severity, action
            );
        }
    }


    //working with feedbacks

    public List<Feedback> viewFeedback() {
       
            return FeedbackManager.getAllFeedbacks();
    }




    public void resolveFeedback(String userID, String feedbackId, String response) {
       
            FeedbackManager.resolveFeedback(userID, feedbackId, response);
            System.out.println("Feedback resolved successfully.");

    }




public void addAdminResponseToFeedback(String userId, String response, String feedbackId) {
    Optional<Feedback> feedbackOpt = FeedbackManager.findByUserAndId(userId, feedbackId);
    
    if (feedbackOpt.isPresent()) {
        Feedback feedback = feedbackOpt.get();
        feedback.addAdminResponse(response);
    } else {
        throw new IllegalArgumentException("Feedback not found for user ID: " + userId + " and feedback ID: " + feedbackId);
    }
}



    public List<Feedback> getFeedbacksByType(Feedback.FeedbackType type) {
       
            return FeedbackManager.getFeedbacksByType(type);
    }
    
    
    
}