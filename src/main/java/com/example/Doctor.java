package com.example;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Doctor extends User {
    private final String licenseNumber;
    private final Specialization specialization;
    private final Set<Qualification> qualifications;
    private final List<TimeSlot> availableSlots;
    private final AppointmentManager appointmentManager;
    private final VitalDatabase vitalDatabase;
    private final String doctorID;
    private final ChatServer chatServer;

    public Doctor(String userId, String firstName, String lastName, String email,
                 String password, LocalDate dateOfBirth, String address,
                 String phoneNumber, Gender gender, String nationality,
                 User.EmergencyContact emergencyContact, BloodType bloodType,
                 String identificationNumber, String preferredLanguage,
                 String profileImageUrl, String licenseNumber,
                 Specialization specialization, AppointmentManager appointmentManager, VitalDatabase vitalDatabase,
                 FeedbackManager feedbackManager, ChatServer chatserver) {

        super(userId, firstName, lastName, email, Role.DOCTOR, password,
              dateOfBirth, address, phoneNumber, gender, nationality,
              emergencyContact, bloodType, identificationNumber,
              preferredLanguage, profileImageUrl,feedbackManager);

        this.doctorID = "D-" + UUID.randomUUID().toString().substring(0, 8);
        this.licenseNumber = licenseNumber;
        this.specialization = Objects.requireNonNull(specialization);
        this.qualifications = ConcurrentHashMap.newKeySet();
        this.availableSlots = new CopyOnWriteArrayList<>();
        this.appointmentManager = appointmentManager;
        this.vitalDatabase = vitalDatabase;
        this.chatServer=chatserver;
    }

    public void startConsultation(Patient patient) {
        System.out.printf("Dr. %s starting consultation with %s%n", getFullName(), patient.getFullName());
        this.connectToChat(chatServer);
        patient.connectToChat(chatServer);
        
        // Send welcome message
        this.sendMessage(patient.getPatientID(), "Hello, I'm Dr. " + getFullName());
    }

    public void endConsultation(Patient patient) {
        System.out.printf("Dr. %s ending consultation with %s%n", getFullName(), patient.getFullName());
        this.sendMessage(patient.getPatientID(), "Consultation completed");
        this.disconnectFromChat();
        patient.disconnectFromChat();
    }
    
    public void addAvailableSlot(LocalDateTime startTime, Duration duration) {
        availableSlots.add(new TimeSlot(startTime, duration));
    }

    public void reviewLatestVitals(Patient patient) {
        Optional<VitalSigns> latestVitals = vitalDatabase.getLatestVitals(patient.getPatientID());

        if (latestVitals.isPresent()) {
            System.out.println("Latest Vital Signs for " + patient.getFullName() + ": " + latestVitals.get());
        } else {
            System.out.println("No vital signs available for patient " + patient.getFullName());
        }
    }

    public void reviewVitalTrends(Patient patient, VitalSigns.Component component, Period lookbackPeriod) {
        Map<String, Double> trends = vitalDatabase.calculateTrends(patient.getPatientID(), component, lookbackPeriod);
        System.out.println("Vital Trends for " + patient.getFullName() + ":");
        trends.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    public boolean recordVitals(Patient patient, VitalSigns vitals) {
        boolean success = vitalDatabase.recordVitals(patient, vitals);
        if (success) {
            System.out.println("Vital signs successfully recorded for " + patient.getFullName());
        } else {
            System.out.println("Failed to record vital signs for " + patient.getFullName() + " (duplicate entry for today).");
        }
        return success;
    }

    public boolean hasCriticalVitals(Patient patient) {
        Optional<VitalSigns> latestVitals = vitalDatabase.getLatestVitals(patient.getPatientID());
        if (latestVitals.isPresent() && latestVitals.get().isCritical()) {
            System.out.println("Critical vital signs detected for " + patient.getFullName());
            return true;
        } else {
            System.out.println("No critical vital signs for " + patient.getFullName());
            return false;
        }
    }

    public void viewVitalsHistory(Patient patient, LocalDate start, LocalDate end) {
        List<VitalSigns> vitalsInRange = vitalDatabase.getVitalsInRange(patient.getPatientID(), start, end);
        if (!vitalsInRange.isEmpty()) {
            System.out.println("Vitals history for " + patient.getFullName() + " between " + start + " and " + end + ":");
            vitalsInRange.forEach(System.out::println);
        } else {
            System.out.println("No vital records found for " + patient.getFullName() + " in the given date range.");
        }
    }

    public void viewTodaySchedule() {
        List<Appointment> todayAppointments = appointmentManager.getDoctorSchedule(doctorID, LocalDate.now());
        if (todayAppointments.isEmpty()) {
            System.out.println("No appointments scheduled for today.");
        } else {
            System.out.println("Today's Appointments:");
            todayAppointments.forEach(System.out::println);
        }
    }

    public void viewScheduleOnDate(LocalDate date) {
        List<Appointment> schedule = appointmentManager.getDoctorSchedule(doctorID, date);
        if (schedule.isEmpty()) {
            System.out.println("No appointments scheduled for " + date);
        } else {
            System.out.println("Appointments on " + date + ":");
            schedule.forEach(System.out::println);
        }
    }
    public void confirmAppointment(String appointmentId) {
        try {
            appointmentManager.confirmAppointment(this.doctorID, appointmentId);
            System.out.println("Appointment confirmed: " + appointmentId);
        } catch (Exception e) {
            System.err.println("Failed to confirm appointment: " + e.getMessage());
            throw new IllegalArgumentException("Unable to confirm appointment", e);
        }
    }
  
    public void cancelAppointment(String appointmentId, String reason) {
        try {
            appointmentManager.cancelAppointmentByDoctor(this.doctorID, appointmentId, reason);
            System.out.println("Appointment cancelled: " + appointmentId);
        } catch (Exception e) {
            System.err.println("Failed to cancel appointment: " + e.getMessage());
        }
    }

    public void rescheduleAppointment(String appointmentId, LocalDateTime newDateTime, Duration newDuration) {
        try {
            Appointment updated = appointmentManager.rescheduleAppointment(appointmentId, newDateTime, newDuration, getFullName());
            System.out.println("Appointment rescheduled: " + updated);
        } catch (Exception e) {
            System.err.println("Failed to reschedule appointment: " + e.getMessage());
        }
    }

    public void viewAppointment(String appointmentId) {
        try {
            Appointment appointment = appointmentManager.getAppointment(appointmentId);
            System.out.println("Appointment Details: " + appointment);
        } catch (Exception e) {
            System.err.println("Error retrieving appointment: " + e.getMessage());
        }
    }

    public Prescription prescribeMedication(String prescriptionId,
                                            Prescription.Medication medication,
                                            LocalDate startDate,
                                            Prescription.Dosage dosage,
                                            String instructions,
                                            Prescription.RefillInfo refillInfo,
                                            String reason, Patient patient) {
        LocalDate today = LocalDate.now();

        Prescription prescription = new Prescription(
            prescriptionId,
            medication,
            this,
            today,
            startDate,
            dosage,
            instructions,
            refillInfo,
            reason
        );

        patient.getPrescriptions().add(prescription);
        System.out.println("Prescription created: " + prescriptionId);
        return prescription;
    }

   

    public void cancelPrescription(String prescriptionId, Patient patient) {
        Prescription prescription = getPrescription(prescriptionId, patient);
        prescription.updateStatus(Prescription.PrescriptionStatus.CANCELLED);
        System.out.println("Prescription cancelled: " + prescriptionId);
    }

    public void completePrescription(String prescriptionId, Patient patient) {
        Prescription prescription = getPrescription(prescriptionId, patient);
        prescription.updateStatus(Prescription.PrescriptionStatus.COMPLETED);
        System.out.println("Prescription completed: " + prescriptionId);
    }

    public void viewPrescription(String prescriptionId, Patient patient) {
        Prescription prescription = getPrescription(prescriptionId, patient);
        System.out.println(prescription);
    }

    public void viewActivePrescriptions(Patient patient) {
        patient.getActivePrescriptions().stream()
            .filter(Prescription::isActive)
            .forEach(System.out::println);
    }

    public void viewAllPrescriptions(Patient patient) {
        patient.getPrescriptions().forEach(System.out::println);
    }

    private Prescription getPrescription(String prescriptionId, Patient patient) {
        return patient.getPrescriptions().stream()
            .filter(x -> x.getID().equals(prescriptionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Prescription not found: " + prescriptionId));
    }

    // Getters
    public String getLicenseNumber() { return licenseNumber; }
    public Specialization getSpecialization() { return specialization; }
    public Set<Qualification> getQualifications() { return Collections.unmodifiableSet(qualifications); }
    public List<TimeSlot> getAvailableSlots() { return Collections.unmodifiableList(availableSlots); }
    public String getDoctorID() { return doctorID; }


    
    @Override
    public void displayInfo() {
        System.out.println("\n=== DOCTOR PROFILE ===");
        System.out.printf("Dr. %s %s (%s)\n", getFirstName(), getLastName(), licenseNumber);
        System.out.println("Specialization: " + specialization);
        System.out.println("Qualifications:");
        qualifications.forEach(q ->
            System.out.printf("- %s (%s, %s, %s)\n",
                q.getName(), q.getSpecialization(),
                q.getObtainedDate(), q.getInstitution()
            )
        );
    }

    // Enums and Inner Classes
    public enum Specialization {
        CARDIOLOGY, NEUROLOGY, ONCOLOGY, PEDIATRICS,
        ORTHOPEDICS, DERMATOLOGY, PSYCHIATRY, GENERAL
    }

    public static class Qualification {
        private final String name;
        private final Specialization specialization;
        private final LocalDate obtainedDate;
        private final String institution;

        public Qualification(String name, Specialization specialization,
                             LocalDate obtainedDate, String institution) {
            this.name = Objects.requireNonNull(name);
            this.specialization = Objects.requireNonNull(specialization);
            this.obtainedDate = Objects.requireNonNull(obtainedDate);
            this.institution = Objects.requireNonNull(institution);
        }

        public String getName() { return name; }
        public Specialization getSpecialization() { return specialization; }
        public LocalDate getObtainedDate() { return obtainedDate; }
        public String getInstitution() { return institution; }
    }

    public static class TimeSlot {
        private final LocalDateTime startTime;
        private final Duration duration;

        public TimeSlot(LocalDateTime startTime, Duration duration) {
            this.startTime = Objects.requireNonNull(startTime);
            this.duration = Objects.requireNonNull(duration);
        }

        public boolean contains(LocalDateTime time, Duration requestedDuration) {
            LocalDateTime endTime = startTime.plus(duration);
            LocalDateTime requestedEnd = time.plus(requestedDuration);
            return !time.isBefore(startTime) && !requestedEnd.isAfter(endTime);
        }
    }


    public List<Feedback> viewFeedback() {
       
            return FeedbackManager.getFeedbacksForDoctor(doctorID);
       
    }

    public List<Feedback> getRecentFeedback(int limit) {
      
            return FeedbackManager.getRecentFeedbackForDoctor(doctorID, limit);
        
    }

    public double getAverageRating() {
       
            return FeedbackManager.getAverageRatingForDoctor(doctorID);
    
    }
    
    
}

