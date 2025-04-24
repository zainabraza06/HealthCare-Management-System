package com.example;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


public class Feedback {
    // ========== PROPERTIES ==========
    private final String feedbackId;
    private final String senderId;   // Patient, staff, etc.
    private final String targetId;   // Doctor, appointment, facility ID
    private final FeedbackType type;
    private final int rating;        // 1-5 scale
    private final String comments;
    private final LocalDateTime submissionDate;

    private FeedbackStatus status;
     String adminResponse;
    private LocalDateTime responseDate;

    // ========== CONSTRUCTOR ==========
    public Feedback(String senderId, String targetId, FeedbackType type, int rating, String comments) {
        this.feedbackId = "FB-" + UUID.randomUUID().toString().substring(0, 8);
        this.senderId = validateId(senderId);
        this.targetId = validateId(targetId);
        this.type = Objects.requireNonNull(type, "Feedback type cannot be null");
        this.rating = validateRating(rating);
        this.comments = Objects.requireNonNull(comments, "Comments cannot be null").trim();
        this.submissionDate = LocalDateTime.now();
        this.status = FeedbackStatus.SUBMITTED;
    }

    // ========== VALIDATION ==========
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        return id.trim();
    }

    private int validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return rating;
    }

    // BUSINESS LOGIC 
    public void addAdminResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin response cannot be empty");
        }
        this.adminResponse = response.trim();
        this.responseDate = LocalDateTime.now();
        this.status = FeedbackStatus.REVIEWED;
    }

    public void markAsResolved(String response) {
        if (adminResponse == null) {
            throw new IllegalStateException("Cannot resolve feedback without admin response");
        }
        this.status = FeedbackStatus.RESOLVED;
        this.adminResponse=response;
    }

    public boolean isPositive() {
        return rating >= 4;
    }

    public boolean requiresFollowUp() {
        return rating <= 2 || comments.toLowerCase().contains("urgent");
    }

    

    public String generateDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Feedback Details ===\n")
          .append("ID: ").append(feedbackId).append("\n")
          .append("Type: ").append(type.getDisplayName()).append("\n")
          .append("From: ").append(senderId).append("\n")
          .append("About: ").append(targetId).append("\n")
          .append("Submitted: ").append(submissionDate).append("\n\n")
          .append("Rating: ").append(rating).append("/5\n\n")
          .append("Comments:\n").append(comments).append("\n");

        if (adminResponse != null) {
            sb.append("\nAdmin Response (").append(responseDate).append("):\n").append(adminResponse).append("\n");
        }

        sb.append("\nStatus: ").append(status.getDisplayName()).append("\n");
        return sb.toString();
    }

    //ENUMS 
    public enum FeedbackType {
        DOCTOR("Doctor Feedback"),
        FACILITY("Facility Feedback"),
        APPOINTMENT("Appointment Feedback"),
        MEDICATION("Medication Feedback"),
        STAFF("Staff Feedback"),
        GENERAL("General Feedback");

        private final String displayName;

        FeedbackType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FeedbackStatus {
        SUBMITTED("Submitted"),
        REVIEWED("Reviewed"),
        RESOLVED("Resolved");

        private final String displayName;

        FeedbackStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========== GETTERS & SETTERS ==========
    public String getFeedbackId() { return feedbackId; }
    public String getSenderId() { return senderId; }
    public String getTargetId() { return targetId; }
    public FeedbackType getType() { return type; }
    public int getRating() { return rating; }
    public String getComments() { return comments; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public FeedbackStatus getStatus() { return status; }
    public String getAdminResponse() { return adminResponse; }
    public LocalDateTime getResponseDate() { return responseDate; }
    public void setStatus(FeedbackStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Feedback[ID=%s, Type=%s, Rating=%d, Status=%s]",
                feedbackId, type, rating, status);
    }
}
