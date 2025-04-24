package com.example;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AppointmentManager {
    private final Map<String, Appointment> appointments = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> doctorSchedules = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> patientSchedules = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Appointment>> pendingAppointments = new ConcurrentHashMap<>();

    private final Map<String, Patient> patients;
    private final Map<String, Doctor> doctors;
    private final ReminderService reminderService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public AppointmentManager(ReminderService reminderService,
                              Map<String, Patient> patients,
                              Map<String, Doctor> doctors) {
        this.reminderService = reminderService;
        this.patients = patients;
        this.doctors = doctors;
    }

    // ========== Appointment Lifecycle Management ==========

    public Appointment scheduleAppointment(String patientId, String doctorId,
                                           LocalDateTime dateTime, Duration duration,
                                           String reason, String location) {
        if (hasConflict(doctorId, dateTime, duration)) {
            throw new IllegalStateException("Schedule conflict detected");
        }

        Appointment appointment = new Appointment(patientId, doctorId, dateTime, duration, reason, location);
        appointments.put(appointment.getId(), appointment);
        addPendingAppointment(doctorId, appointment);
        notifyStatusChange(appointment, Appointment.Status.SCHEDULED);
        scheduleReminders(appointment);

        return appointment;
    }

    public void confirmAppointment(String doctorId, String appointmentId) {
        Appointment appointment = validateAppointmentOwnership(doctorId, appointmentId);
        appointment.updateStatus(Appointment.Status.CONFIRMED, "Doctor confirmed");
        removePendingAppointment(doctorId, appointmentId);
        addToSchedules(appointment);
        notifyStatusChange(appointment, Appointment.Status.CONFIRMED);
    }

    public void cancelAppointmentByDoctor(String doctorId, String appointmentId, String reason) {
        Appointment appointment = validateAppointmentOwnership(doctorId, appointmentId);
        if (!appointment.isActiveStatus()) {
            throw new IllegalStateException("Cannot cancel inactive appointment");
        }
        appointment.updateStatus(Appointment.Status.CANCELLED, reason);
        removeFromSchedules(appointment);
        notifyStatusChange(appointment, Appointment.Status.CANCELLED);
    }

    public void cancelAppointmentByPatient(String patientId, String appointmentId, String reason) {
        Appointment appointment = getAppointment(appointmentId);
        if (!appointment.getPatientId().equals(patientId)) {
            throw new IllegalStateException("Appointment doesn't belong to patient");
        }
        appointment.updateStatus(Appointment.Status.CANCELLED, reason);
        removeFromSchedules(appointment);
        notifyStatusChange(appointment, Appointment.Status.CANCELLED);
    }

    public Appointment rescheduleAppointment(String appointmentId, LocalDateTime newDateTime,
                                             Duration newDuration, String requestedBy) {
        Appointment original = getAppointment(appointmentId);
        if (original.getStatus() == Appointment.Status.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule cancelled appointment");
        }

        if (hasConflict(original.getDoctorId(), newDateTime, newDuration, appointmentId)) {
            throw new IllegalStateException("New time conflicts with existing appointments");
        }

        Appointment rescheduled = createRescheduledAppointment(original, newDateTime, newDuration);
        updateOriginalAppointmentStatus(original, newDateTime, requestedBy);
        notifyStatusChange(rescheduled, Appointment.Status.RESCHEDULED);
        scheduleReminders(rescheduled);

        return rescheduled;
    }

    public void startAppointment(String doctorId, String appointmentId) {
        Appointment appointment = validateAppointmentOwnership(doctorId, appointmentId);
        if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointments can be started");
        }
        if (LocalDateTime.now().isAfter(appointment.getDateTime().plus(appointment.getDuration()))) {
            throw new IllegalStateException("Cannot start an appointment that already ended");
        }
        appointment.updateStatus(Appointment.Status.IN_PROGRESS, "Patient arrived");
        notifyStatusChange(appointment, Appointment.Status.IN_PROGRESS);
    }

    public void completeAppointment(String doctorId, String appointmentId, String clinicalNotes) {
        Appointment appointment = validateAppointmentOwnership(doctorId, appointmentId);
        if (appointment.getStatus() != Appointment.Status.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress appointments can be completed");
        }
        appointment.updateStatus(Appointment.Status.COMPLETED, "Visit completed");
        removeFromSchedules(appointment);
        notifyStatusChange(appointment, Appointment.Status.COMPLETED);
    }

    public void markAsNoShow(String doctorId, String appointmentId) {
        Appointment appointment = validateAppointmentOwnership(doctorId, appointmentId);
        if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointments can be marked as no-show");
        }
        if (LocalDateTime.now().isBefore(appointment.getDateTime().plus(appointment.getDuration()))) {
            throw new IllegalStateException("Too early to mark as no-show");
        }
        appointment.updateStatus(Appointment.Status.NO_SHOW, "Patient didn't arrive");
        removeFromSchedules(appointment);
        notifyStatusChange(appointment, Appointment.Status.NO_SHOW);
    }

    // ========== Schedule Queries ==========

    public List<Appointment> getDoctorSchedule(String doctorId, LocalDate date) {
        return getScheduleForDate(doctorId, date, doctorSchedules);
    }

    public List<Appointment> getPatientSchedule(String patientId, LocalDate date) {
        return getScheduleForDate(patientId, date, patientSchedules);
    }

    public List<Appointment> getDoctorsActiveAppointments(String doctorId) {
        return doctorSchedules.getOrDefault(doctorId, Collections.emptySet()).stream()
            .map(appointments::get)
            .filter(Objects::nonNull)
            .filter(a -> a.getStatus() == Appointment.Status.IN_PROGRESS)
            .collect(Collectors.toList());
    }

    public Appointment getAppointment(String appointmentId) {
        Appointment appointment = appointments.get(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found");
        }
        return appointment;
    }

    // ========== Reminder System ==========

    private void scheduleReminders(Appointment appointment) {
        scheduleReminder(appointment, appointment.getDateTime().minusHours(24));
        scheduleReminder(appointment, appointment.getDateTime().minusHours(1));
    }

    private void scheduleReminder(Appointment appointment, LocalDateTime reminderTime) {
        long delay = Math.max(Duration.between(LocalDateTime.now(), reminderTime).toMillis(), 0);
        Patient patient = patients.get(appointment.getPatientId());
        scheduler.schedule(() -> reminderService.sendAppointmentReminder(appointment, patient), delay, TimeUnit.MILLISECONDS);
    }

    private void notifyStatusChange(Appointment appointment, Appointment.Status status) {
        Patient patient = patients.get(appointment.getPatientId());
        reminderService.sendStatusNotification(appointment, status, patient);
    }

    // ========== Internal Helpers ==========

    private boolean hasConflict(String doctorId, LocalDateTime dateTime, Duration duration) {
        return getDoctorSchedule(doctorId, dateTime.toLocalDate()).stream()
            .anyMatch(existing -> isTimeConflict(dateTime, duration, existing));
    }

    private boolean hasConflict(String doctorId, LocalDateTime dateTime, Duration duration, String excludeId) {
        return doctorSchedules.getOrDefault(doctorId, Collections.emptySet()).stream()
            .filter(id -> !id.equals(excludeId))
            .map(appointments::get)
            .filter(Objects::nonNull)
            .anyMatch(existing -> isTimeConflict(dateTime, duration, existing));
    }

    private boolean isTimeConflict(LocalDateTime start, Duration duration, Appointment existing) {
        LocalDateTime end = start.plus(duration);
        LocalDateTime existingEnd = existing.getDateTime().plus(existing.getDuration());
        return start.isBefore(existingEnd) && existing.getDateTime().isBefore(end);
    }

    private List<Appointment> getScheduleForDate(String id, LocalDate date, Map<String, Set<String>> scheduleMap) {
        return scheduleMap.getOrDefault(id, Collections.emptySet()).stream()
            .map(appointments::get)
            .filter(Objects::nonNull)
            .filter(a -> a.getDateTime().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }

    private void addPendingAppointment(String doctorId, Appointment appointment) {
        pendingAppointments.computeIfAbsent(doctorId, k -> new ConcurrentHashMap<>())
            .put(appointment.getId(), appointment);
    }

    private void removePendingAppointment(String doctorId, String appointmentId) {
        Map<String, Appointment> pending = pendingAppointments.get(doctorId);
        if (pending != null) {
            pending.remove(appointmentId);
            if (pending.isEmpty()) {
                pendingAppointments.remove(doctorId);
            }
        }
    }

    private void addToSchedules(Appointment appointment) {
        addToSchedule(appointment.getDoctorId(), appointment.getId(), doctorSchedules);
        addToSchedule(appointment.getPatientId(), appointment.getId(), patientSchedules);
    }

    private void removeFromSchedules(Appointment appointment) {
        removeFromSchedule(appointment.getDoctorId(), appointment.getId(), doctorSchedules);
        removeFromSchedule(appointment.getPatientId(), appointment.getId(), patientSchedules);
    }

    private void addToSchedule(String id, String appointmentId, Map<String, Set<String>> map) {
        map.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(appointmentId);
    }

    private void removeFromSchedule(String id, String appointmentId, Map<String, Set<String>> map) {
        Set<String> schedule = map.get(id);
        if (schedule != null) {
            schedule.remove(appointmentId);
        }
    }

    private Appointment createRescheduledAppointment(Appointment original, LocalDateTime newTime, Duration newDuration) {
        Appointment rescheduled = new Appointment(
            original.getPatientId(), original.getDoctorId(), newTime, newDuration,
            original.getReason() + " (Rescheduled from " + original.getDateTime() + ")",
            original.getLocation()
        );
        appointments.put(rescheduled.getId(), rescheduled);
        addPendingAppointment(original.getDoctorId(), rescheduled);
        return rescheduled;
    }

    private void updateOriginalAppointmentStatus(Appointment original, LocalDateTime newTime, String requestedBy) {
        original.updateStatus(Appointment.Status.RESCHEDULED, "Rescheduled to " + newTime + " by " + requestedBy);
    }

    private Appointment validateAppointmentOwnership(String doctorId, String appointmentId) {
        Appointment appointment = getAppointment(appointmentId);
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new IllegalStateException("Appointment doesn't belong to this doctor");
        }
        return appointment;
    }
}
