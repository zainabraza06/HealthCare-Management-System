package com.example;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZoomProvider implements VideoProvider {
    private static final String BASE_URL = "https://zoom.us/j/";
    private final Map<String, ZoomMeeting> meetings = new HashMap<>();

    @Override
    public String createMeeting(String title, LocalDateTime scheduledTime) {
        String meetingId = "zm-" + UUID.randomUUID().toString();
        long meetingNumber = Math.abs(UUID.randomUUID().getMostSignificantBits());
        String joinUrl = BASE_URL + meetingNumber;
        
        meetings.put(meetingId, new ZoomMeeting(meetingId, joinUrl, title, scheduledTime, meetingNumber));
        return joinUrl;
    }

    @Override
    public void endMeeting(String meetingId) {
        ZoomMeeting meeting = meetings.remove(meetingId);
        if (meeting != null) {
            System.out.println("Ended Zoom meeting #" + meeting.meetingNumber);
        }
    }

    @Override
    public Map<String, String> getMeetingDetails(String meetingId) {
        ZoomMeeting meeting = meetings.get(meetingId);
        if (meeting == null) {
            return null;
        }
        
        Map<String, String> details = new HashMap<>();
        details.put("meetingId", meeting.meetingId);
        details.put("joinUrl", meeting.joinUrl);
        details.put("title", meeting.title);
        details.put("scheduledTime", meeting.scheduledTime.toString());
        details.put("meetingNumber", String.valueOf(meeting.meetingNumber));
        details.put("password", meeting.password); // In real app, would be encrypted
        return details;
    }

    @Override
    public String generateInstantLink() {
        return BASE_URL + UUID.randomUUID().getMostSignificantBits() + "?pwd=" + 
               UUID.randomUUID().toString().substring(0, 6);
    }

    private static class ZoomMeeting {
        final String meetingId;
        final String joinUrl;
        final String title;
        final LocalDateTime scheduledTime;
        final long meetingNumber;
        final String password;

        ZoomMeeting(String meetingId, String joinUrl, String title, 
                   LocalDateTime scheduledTime, long meetingNumber) {
            this.meetingId = meetingId;
            this.joinUrl = joinUrl;
            this.title = title;
            this.scheduledTime = scheduledTime;
            this.meetingNumber = meetingNumber;
            this.password = UUID.randomUUID().toString().substring(0, 6);
        }
    }
}