package com.example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class VideoCallService {
    private final Map<String, VideoCallSession> activeSessions = new ConcurrentHashMap<>();
    private final VideoProvider videoProvider;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

    public VideoCallService(VideoProvider videoProvider) {
        this.videoProvider = Objects.requireNonNull(videoProvider, "VideoProvider cannot be null");
        
        // Clean up old sessions every hour
        cleanupScheduler.scheduleAtFixedRate(this::cleanupOldSessions, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Starts a new video call session for an appointment
     */
    public VideoCallSession startVideoCall(String appointmentId, String initiatorId) {
        if (activeSessions.containsKey(appointmentId)) {
            throw new IllegalStateException("Video call already exists for this appointment");
        }

        String meetingTitle = "Medical Consultation - Appointment " + appointmentId;
        String joinLink = videoProvider.createMeeting(meetingTitle, LocalDateTime.now().plusMinutes(5));

        VideoCallSession session = new VideoCallSession(
            UUID.randomUUID().toString(),
            appointmentId,
            joinLink,
            initiatorId,
            LocalDateTime.now()
        );

        activeSessions.put(appointmentId, session);
        return session;
    }

    /**
     * Joins an existing video call
     */
    public String joinVideoCall(String appointmentId, String participantId) {
        VideoCallSession session = activeSessions.get(appointmentId);
        if (session == null) {
            throw new IllegalArgumentException("No active video call for this appointment");
        }

        session.addParticipant(participantId);
        return session.getJoinLink();
    }

    /**
     * Ends a video call session
     */
    public void endVideoCall(String appointmentId, String initiatorId) {
        VideoCallSession session = activeSessions.get(appointmentId);
        if (session == null) {
            throw new IllegalArgumentException("No active video call for this appointment");
        }

        if (!session.getInitiatorId().equals(initiatorId)) {
            throw new SecurityException("Only the call initiator can end the session");
        }

        videoProvider.endMeeting(session.getMeetingId());
        session.end();
        activeSessions.remove(appointmentId);
    }

    /**
     * Gets session details
     */
    public VideoCallSession getSession(String appointmentId) {
        return activeSessions.get(appointmentId);
    }

    /**
     * Gets all active sessions (for admin monitoring)
     */
    public List<VideoCallSession> getActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }

    private void cleanupOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getStartTime().isBefore(cutoff)
        );
    }

    public void shutdown() {
        executor.shutdown();
        cleanupScheduler.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Video Call Session Entity
     */
    public static class VideoCallSession {
        private final String meetingId;
        private final String appointmentId;
        private final String joinLink;
        private final String initiatorId;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private final Set<String> participants = ConcurrentHashMap.newKeySet();
        private boolean isActive = true;

        public VideoCallSession(String meetingId, String appointmentId, 
                              String joinLink, String initiatorId, 
                              LocalDateTime startTime) {
            this.meetingId = meetingId;
            this.appointmentId = appointmentId;
            this.joinLink = joinLink;
            this.initiatorId = initiatorId;
            this.startTime = startTime;
            this.participants.add(initiatorId);
        }

        public void addParticipant(String participantId) {
            participants.add(participantId);
        }

        public void end() {
            this.isActive = false;
            this.endTime = LocalDateTime.now();
        }

        // Getters
        public String getMeetingId() { return meetingId; }
        public String getAppointmentId() { return appointmentId; }
        public String getJoinLink() { return joinLink; }
        public String getInitiatorId() { return initiatorId; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public Set<String> getParticipants() { return Collections.unmodifiableSet(participants); }
        public boolean isActive() { return isActive; }
    }
}