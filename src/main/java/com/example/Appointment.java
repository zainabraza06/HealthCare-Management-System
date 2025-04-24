package com.example;

import java.time.*;
import java.util.*;


public class Appointment {
    private final String id;
    private final String patientId;
    private final String doctorId;
    private LocalDateTime dateTime;
    private Duration duration;
    private String reason;
    private String location;
    private Status status;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    

    public Appointment(String patientId, String doctorId, LocalDateTime dateTime, 
                     Duration duration, String reason, String location) {
        this.id = "APT-" + UUID.randomUUID().toString().substring(0, 8);
        this.patientId = validateId(patientId, "Patient ID");
        this.doctorId = validateId(doctorId, "Doctor ID");
        this.dateTime = Objects.requireNonNull(dateTime);
        this.duration = validateDuration(duration);
        this.reason = validateText(reason, "Routine checkup");
        this.location = validateText(location, "Clinic");
        this.status = Status.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }



        //updating status

    public void updateStatus(Status newStatus, String reason) {
        if (!isActiveStatus()) {
            throw new IllegalStateException("Cannot change status from CANCELLED or COMPLETED");
        }
        
        
        if (newStatus == Status.CANCELLED) {
            this.cancellationReason = (reason != null) ? reason : "No reason provided";
        }
    
        this.status = newStatus;
        updateTimestamp();
    }
   
    
    //to check if the appointment is active
    public boolean isActiveStatus() {
        return status != Status.CANCELLED && status != Status.COMPLETED;
    }
    

  
    //validation methods

    private String validateId(String id, String fieldName) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        return id.trim();
    }

    private Duration validateDuration(Duration duration) {
        if (duration.toMinutes() < 15) {
            throw new IllegalArgumentException("Duration must be ≥15 minutes");
        }
        return duration;
    }

    private String validateText(String text, String defaultValue) {
        return (text != null && !text.trim().isEmpty()) ? text.trim() : defaultValue;
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }




    // Getters
    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getDateTime() { return dateTime; }
    public Duration getDuration() { return duration; }
    public LocalDateTime getEndTime() { return dateTime.plus(duration); }
    public String getReason() { return reason; }
    public String getLocation() { return location; }
    public Status getStatus() { return status; }
    public String getCancellationReason() { return cancellationReason; }




    public enum Status {
        SCHEDULED, RESCHEDULED, CONFIRMED, IN_PROGRESS, 
        COMPLETED, CANCELLED, NO_SHOW
    }


}
