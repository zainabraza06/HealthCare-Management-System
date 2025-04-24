package com.example;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


interface VideoProvider {
    /**
     * Creates a new video meeting
     * @param title Meeting title/description
     * @param scheduledTime When the meeting should occur
     * @return Joinable URL for the meeting
     */
    String createMeeting(String title, LocalDateTime scheduledTime);

    /**
     * Ends an active meeting
     * @param meetingId The ID of the meeting to end
     */
    void endMeeting(String meetingId);

    /**
     * Gets meeting details
     * @param meetingId The meeting ID
     * @return Map containing meeting details (joinUrl, startTime, etc.)
     */
    Map<String, String> getMeetingDetails(String meetingId);

    /**
     * Generates a meeting join link without creating a meeting
     * @return Instant join URL
     */
    default String generateInstantLink() {
        throw new UnsupportedOperationException("Instant links not supported");
    }
}



public class GoogleMeetProvider implements VideoProvider {
    private static final String BASE_URL = "https://meet.google.com/";
    private final Map<String, GoogleMeeting> meetings = new HashMap<>();

    @Override
    public String createMeeting(String title, LocalDateTime scheduledTime) {
        String meetingId = "gm-" + UUID.randomUUID().toString().substring(0, 8);
        String meetingCode = generateMeetingCode();
        String joinUrl = BASE_URL + meetingCode;
        
        meetings.put(meetingId, new GoogleMeeting(meetingId, joinUrl, title, scheduledTime));
        return joinUrl;
    }

    @Override
    public void endMeeting(String meetingId) {
        meetings.remove(meetingId);
        // In real implementation, would call Google Calendar API
        System.out.println("Google Meet meeting ended: " + meetingId);
    }

    @Override
    public Map<String, String> getMeetingDetails(String meetingId) {
        GoogleMeeting meeting = meetings.get(meetingId);
        if (meeting == null) {
            return null;
        }
        
        Map<String, String> details = new HashMap<>();
        details.put("meetingId", meeting.meetingId);
        details.put("joinUrl", meeting.joinUrl);
        details.put("title", meeting.title);
        details.put("scheduledTime", meeting.scheduledTime.toString());
        return details;
    }

    @Override
    public String generateInstantLink() {
        return BASE_URL + "new?hs=181"; // Standard instant meeting link
    }

    private String generateMeetingCode() {
        // Simulate Google Meet code generation (3 groups of 3 letters)
        return String.format("%s-%s-%s", 
            randomLetterGroup(), randomLetterGroup(), randomLetterGroup());
    }

    private String randomLetterGroup() {
        return UUID.randomUUID().toString()
            .replaceAll("[^a-z]", "")
            .substring(0, 3);
    }

    private static class GoogleMeeting {
        final String meetingId;
        final String joinUrl;
        final String title;
        final LocalDateTime scheduledTime;

        GoogleMeeting(String meetingId, String joinUrl, String title, LocalDateTime scheduledTime) {
            this.meetingId = meetingId;
            this.joinUrl = joinUrl;
            this.title = title;
            this.scheduledTime = scheduledTime;
        }
    }
}