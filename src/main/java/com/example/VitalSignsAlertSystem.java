package com.example;

import java.time.*;
import java.util.*;

public class VitalSignsAlertSystem {

    private final Map<String, LocalDateTime> alertCooldowns = new HashMap<>();
    private final long cooldownMinutes;
    private final NotificationService notificationService;

    public VitalSignsAlertSystem(long cooldownMinutes, NotificationService notificationService) {
        this.cooldownMinutes = cooldownMinutes;
        this.notificationService = notificationService;
    }

    public void triggerAlert(String patientId, VitalSigns vitals, String doctorEmail, String doctorPhone) {
        if (isOnCooldown(patientId)) return;

        String priority = determinePriority(vitals);

        try {
            if ("HIGH".equals(priority)) {
                notificationService.sendEmergencyAlert(
                    List.of(doctorEmail),
                    List.of(doctorPhone),
                    patientId,
                    "CRITICAL Vital Signs"
                );
            } else {
                notificationService.sendCriticalResults(
                    doctorEmail,
                    doctorPhone,
                    "Patient " + patientId,
                    "Abnormal Vitals",
                    buildSummary(vitals)
                );
            }
            alertCooldowns.put(patientId, LocalDateTime.now());
        } catch (NotificationException e) {
            System.err.println("Alert failed: " + e.getMessage());
        }
    }

    private boolean isOnCooldown(String patientId) {
        LocalDateTime lastAlert = alertCooldowns.get(patientId);
        return lastAlert != null && lastAlert.plusMinutes(cooldownMinutes).isAfter(LocalDateTime.now());
    }

    private String determinePriority(VitalSigns vitals) {
        if (vitals.getOxygenSaturation() < 90 || vitals.getBloodPressure().isCritical()) {
            return "HIGH";
        }
        return "MEDIUM";
    }

    private String buildSummary(VitalSigns vitals) {
        StringBuilder sb = new StringBuilder();

        if (vitals.getBodyTemperature() > 39.0 || vitals.getBodyTemperature() < 35.0) {
            sb.append("Abnormal Temp: ").append(vitals.getBodyTemperature()).append("°C\n");
        }

        if (vitals.getPulseRate() > 120 || vitals.getPulseRate() < 50) {
            sb.append("Abnormal Pulse: ").append(vitals.getPulseRate()).append(" bpm\n");
        }

        if (vitals.getBloodPressure().isCritical()) {
            sb.append("Critical BP: ").append(vitals.getBloodPressure().getBloodPressureReading()).append("\n");
        }

        if (vitals.getOxygenSaturation() < 92) {
            sb.append("Low SpO2: ").append(vitals.getOxygenSaturation()).append("%\n");
        }

        return sb.toString().trim();
    }
}
