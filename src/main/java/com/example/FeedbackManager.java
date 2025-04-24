package com.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class FeedbackManager {

    // Maps user ID to their list of feedbacks
    private static final Map<String, List<Feedback>> feedbackStorage = new HashMap<>();

    // CORE ACTIONS 

    public static void submitFeedback(Feedback feedback) {
        if (feedback == null) {
            return;
        }

        String userId = feedback.getSenderId();
        feedbackStorage.computeIfAbsent(userId, k -> new ArrayList<>()).add(feedback);
    }

//admin meth

    public static void resolveFeedback(String userId, String feedbackId,String response) {
        Feedback feedback = findByUserAndId(userId, feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback ID not found for this user"));

        if (response == null) {
            throw new IllegalStateException("Cannot resolve without admin response.");
        }

        feedback.markAsResolved(response);
    }

  

    // ========== RETRIEVAL METHODS ==========

    public static List<Feedback> getAllFeedbacks() {
        return feedbackStorage.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    

    public static List<Feedback> getFeedbacksByUser(String userId) {
        return new ArrayList<>(feedbackStorage.getOrDefault(userId, new ArrayList<>()));
    }

    public static List<Feedback> getFeedbacksByType(Feedback.FeedbackType type) {
        return feedbackStorage.values().stream()
                .flatMap(List::stream)
                .filter(fb -> fb.getType().equals(type))
                .collect(Collectors.toList());
    }

    public static List<Feedback> getFeedbacksForDoctor(String doctorId) {
        return getAllFeedbacks().stream()
                .filter(fb -> fb.getType() == Feedback.FeedbackType.DOCTOR && fb.getTargetId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public static List<Feedback> getRecentFeedbackForDoctor(String doctorId, int limit) {
        return getFeedbacksForDoctor(doctorId).stream()
                .sorted(Comparator.comparing(Feedback::getSubmissionDate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static double getAverageRatingForDoctor(String doctorId) {
        List<Feedback> feedbacks = getFeedbacksForDoctor(doctorId);
        return feedbacks.isEmpty() ? 0 :
               feedbacks.stream().mapToInt(Feedback::getRating).average().orElse(0);
    }

    // ========== UTILITIES ==========

    public static Optional<Feedback> findByUserAndId(String userId, String feedbackId) {
        return feedbackStorage.getOrDefault(userId, new ArrayList<>()).stream()
                .filter(fb -> fb.getFeedbackId().equals(feedbackId))
                .findFirst();
    }

 
}