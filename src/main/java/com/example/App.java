package com.example;

import java.time.*;
import java.util.*;

public class App {
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        FeedbackManager feedbackManager = new FeedbackManager();
        EmailNotification emailNotification = new EmailNotification("smtp.gmail.com", 587, "shayanmukhtiar77@gmail.com",
                "issvqsnmzbzoazki", "shayanmukhtiar77@gmail.com", false);
        WhatsAppNotification whatsAppNotification = new WhatsAppNotification("678407622012605", "EAAPSCowKTw8BO4C1b1FfqjBbyfHqd7ksYrSIPzlvqa9C3WtcqaMUZBX3HOjiEfZB33ZAy9pu8ynncdvAZAZAvJMU9IUa6gpe8HPS9fzOkQCfsC9Qcn5tjiH5MfNbjc4DBlts2QJn8qdXOsRXd6o37AJDCxrLpZBE1MIZAcqbeiZBW3dAgBEG8SlxuScdn4R0ZApBV6W95mSFbhPg0AqMHgrMuaAzJqtMbgwBkkMoZD");
        ReminderService reminderService = new ReminderService(emailNotification, whatsAppNotification);
        VideoCallService videoCallService = new VideoCallService(new GoogleMeetProvider());
        NotificationService notificationService = new NotificationService(emailNotification, whatsAppNotification);
        VitalSignsAlertSystem alertSystem = new VitalSignsAlertSystem(5, notificationService);
        VitalDatabase vitalDatabase = new VitalDatabase(5, alertSystem);

        Map<String, Patient> patients = new HashMap<>();
        Map<String, Doctor> doctors = new HashMap<>();

        AppointmentManager appointmentManager = new AppointmentManager(
                reminderService,
                patients,
                doctors
        );

        User.EmergencyContact patientEmergencyContact = new User.EmergencyContact(
                "Jane Doe", "555-0102", "Spouse", "zainabraza1960@gmail.com");
        User.EmergencyContact doctorEmergencyContact = new User.EmergencyContact(
                "Clinic Admin", "+1-555-0202", "Sister", "zainabrazamalikse@gmail.com");

        Patient patient = new Patient(
                "00001", "John", "Doe", "zainabraza1960@gmail.com", "patient123",
                LocalDate.of(1985, 5, 15), "123 Main St", "555-0101", User.Gender.MALE,
                "US", patientEmergencyContact, User.BloodType.A_POSITIVE,
                "123-45-6789", "English", "profile.jpg", "PAT001", "MRN00001",
                new MedicalRecord(), null, appointmentManager, "Main Clinic",
                vitalDatabase, feedbackManager, chatServer
        );
        patients.put(patient.getPatientID(), patient);

        Doctor doctor = new Doctor(
                "00002", "Sarah", "Smith", "faizamalik60ai@gmail.com", "secureDoctorPassword",
                LocalDate.of(1975, 8, 20), "456 Oak Avenue, Medical Center", "+1-555-0201",
                User.Gender.FEMALE, "United Kingdom", doctorEmergencyContact,
                User.BloodType.O_POSITIVE, "DOC-987-65-4321", "English",
                "https://clinic.com/profiles/dr_smith.jpg", "MD1234567",
                Doctor.Specialization.CARDIOLOGY, appointmentManager, vitalDatabase,
                feedbackManager, chatServer
        );
        doctors.put(doctor.getDoctorID(), doctor);

        patient.setPrimaryDoctor(doctor);

        Admin admin = new Admin(
                "00003", "System", "Admin", "admin@healthcare.com", "admin123",
                LocalDate.of(1980, 1, 1), "789 Admin Blvd", "555-0000", User.Gender.OTHER,
                "US", new User.EmergencyContact("Security", "555-0001", "Office", "zainabraza1960@gmail.com"),
                User.BloodType.AB_POSITIVE, "ADM-001", "English", "admin.jpg", feedbackManager
        );

        Appointment scheduledAppointment = testAppointments(appointmentManager, doctor, patient);
        testChatSystem(chatServer, doctor, patient);
        testVideoConsultation(videoCallService, doctor, patient);
        testPrescriptionSystem(doctor, patient);
        testVitalSigns(vitalDatabase, patient);
        testFeedbackSystem(feedbackManager, doctor, patient);
        testNotifications(reminderService, scheduledAppointment, patient);
        testEmergencyFeatures(alertSystem, whatsAppNotification, emailNotification, patient);
        testMedicalRecords(doctor, patient);

        shutdownServices(chatServer, videoCallService);
    }

    private static Appointment testAppointments(AppointmentManager manager, Doctor doctor, Patient patient) {
        System.out.println("\n=== Testing Appointment System ===");
        Appointment appointment = manager.scheduleAppointment(
                patient.getPatientID(),
                doctor.getDoctorID(),
                LocalDateTime.now().plusDays(1),
                Duration.ofMinutes(30),
                "Annual checkup",
                "Main Clinic"
        );
        System.out.println("Scheduled appointment: " + appointment.getId());

        manager.confirmAppointment(doctor.getDoctorID(), appointment.getId());
        System.out.println("Confirmed appointment");

        System.out.println("Doctor's schedule tomorrow:");
        manager.getDoctorSchedule(doctor.getDoctorID(), LocalDate.now().plusDays(1))
              .forEach(a -> System.out.println("- " + a.getDateTime() + ": " + a.getReason()));

        return appointment;
    }
    private static void testChatSystem(ChatServer server, Doctor doctor, Patient patient) {
        System.out.println("\n=== Testing Chat System ===");
        ChatClient doctorClient = new ChatClient(doctor.getDoctorID(), server);
        ChatClient patientClient = new ChatClient(patient.getPatientID(), server);
        
        new Thread(doctorClient).start();
        new Thread(patientClient).start();
        
        doctorClient.sendMessage(patient.getPatientID(), "Hello, how can I help you today?");
        patientClient.sendMessage(doctor.getDoctorID(), "I've been having headaches");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        doctorClient.disconnect();
        patientClient.disconnect();
    }

    private static void testVideoConsultation(VideoCallService service, Doctor doctor, Patient patient) {
        System.out.println("\n=== Testing Video Consultation ===");
        VideoCallService.VideoCallSession session = service.startVideoCall("apt1", doctor.getDoctorID());
        System.out.println("Video call started. Join link: " + session.getJoinLink());
        
        System.out.println(patient.getFirstName() + " joining video call...");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        service.endVideoCall("apt1", doctor.getDoctorID());
        System.out.println("Video consultation ended");
    }

    private static void testPrescriptionSystem(Doctor doctor, Patient patient) {
        System.out.println("\n=== Testing Prescription System ===");
    
        // Create medication
        Prescription.Medication ibuprofen = new Prescription.Medication(
            "Ibuprofen",
            "Ibuprofen",
            "1234-5678-90",
            Prescription.MedicationClass.ANALGESIC,
            "tablet",
            "200mg"
        );
    
        // Create dosage
        Prescription.Dosage dosage = new Prescription.Dosage(
            1,                          // amount
            "tablet",                   // unit
            Prescription.Frequency.BID, // frequency
            "oral"                      // route
        );
    
        // Create refill info
        Prescription.RefillInfo refillInfo = new Prescription.RefillInfo(2, 2);
    
        // Create prescription
        Prescription prescription = new Prescription(
            "RX12345678",
            ibuprofen,
            doctor,
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            dosage,
            "Take with food",
            refillInfo,
            "For headache relief"
        );
    
        // Add prescription to patient
        patient.addPrescription(prescription);
    
        // Attempt to dispense the prescription
        try {
            prescription.dispense("CVS Pharmacy #1234, Main Street");
            System.out.println("Prescription dispensed successfully.");
        } catch (IllegalStateException e) {
            System.out.println("Error dispensing: " + e.getMessage());
        }
    
        // Print details
        System.out.println("\nPrescription Details:");
        System.out.println("- ID: " + prescription.getID());
        System.out.println("- Medication: " + prescription.getMedication().getName());
        System.out.println("- Dosage: " + prescription.getDosage().amount() + " " +
                           prescription.getDosage().unit() + " " +
                           prescription.getDosage().frequency().getDescription());
        System.out.println("- Status: " + prescription.getStatus());
    
        // Refill the prescription
        try {
            prescription.refill();
            System.out.println("Prescription refilled.");
        } catch (IllegalStateException e) {
            System.out.println("Error refilling: " + e.getMessage());
        }
    
        // Display active prescriptions
        System.out.println("\nPatient's active prescriptions:");
        for (Prescription p : patient.getActivePrescriptions()) {
            System.out.println("- " + p.getMedication().getName() + " (" + p.getStatus() + ")");
        }
    }
    
    private static void testVitalSigns(VitalDatabase database, Patient patient) {
        System.out.println("\n=== Testing Vital Signs ===");
        VitalSigns.BloodPressure bp = new VitalSigns.BloodPressure(135, 85);
VitalSigns vitals = new VitalSigns(
    LocalDateTime.now(),
    37.2,
    78,
    18,
    bp,
    96.5,
    175.0,
    72.0
);
vitals.setPainLevel(VitalSigns.PainLevel.MODERATE);
        
        database.recordVitals(patient, vitals);
        System.out.println("Recorded vitals for " + patient.getFirstName());
        
        Optional<VitalSigns> latest = database.getLatestVitals(patient.getPatientID());
        latest.ifPresent(v -> System.out.println("Latest vitals: " + v));
    }
    private static void testFeedbackSystem(FeedbackManager manager, Doctor doctor, Patient patient) {
        System.out.println("\n=== Testing Feedback System ===");
    
        // Create Feedback object manually
        Feedback feedback = new Feedback(
            patient.getPatientID(),             // senderId
            doctor.getDoctorID(),               // targetId
            Feedback.FeedbackType.DOCTOR,       // type
            5,                                  // rating
            "Very thorough and professional"    // comments
        );
    
        // Submit feedback through patient
        patient.submitFeedback(feedback);
    
        // Print confirmation and rating
        System.out.println("Submitted feedback: " + feedback.getRating() + " stars");
        System.out.println("Doctor's average rating: " +
        FeedbackManager.getAverageRatingForDoctor(doctor.getDoctorID()));
    }
    

    private static void testNotifications(ReminderService service, Appointment appointment, Patient patient) {
        System.out.println("\n=== Testing Notifications ===");
        service.sendAppointmentReminder(appointment, patient);
        System.out.println("Sent appointment reminder");
        
        // Create a medication reminder
        Prescription.Medication vitaminD = new Prescription.Medication(
            "Vitamin D", 
            "Vitamin D3", 
            "4321-8765-09", 
            Prescription.MedicationClass.OTHER, 
            "tablet", 
            "1000IU"
        );
        
        Prescription.Dosage vitDDosage = new Prescription.Dosage(
            1, 
            "tablet", 
            Prescription.Frequency.QD, 
            "oral"
        );
        
        Prescription vitDPrescription = new Prescription(
            "RX87654321",
            vitaminD,
            patient.getPrimaryDoctor(),
            LocalDate.now(),
            LocalDate.now(),
            vitDDosage,
            "Take in morning",
            null,
            "General health"
        );
        
        service.sendMedicationReminder(vitDPrescription, patient);
        System.out.println("Sent medication reminder");
    }

    private static void testEmergencyFeatures(VitalSignsAlertSystem service,WhatsAppNotification whatsAppNotification, EmailNotification emailNotification, Patient patient) {
        System.out.println("\n=== Testing Emergency Features ===");
    
        // Simulate panic button
        PanicButton panicButton = new PanicButton(patient, whatsAppNotification, emailNotification);
        try {
            panicButton.activate();
            System.out.println("Panic button activated.");
        } catch (NotificationException e) {
            System.err.println("Failed to send panic button alert: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Panic button could not be activated: " + e.getMessage());
        }
    
        // Manually create critical vital signs
        VitalSigns.BloodPressure criticalBP = new VitalSigns.BloodPressure(185, 125); // Hypertensive crisis
        VitalSigns criticalVitals = new VitalSigns(
            LocalDateTime.now(),
            40.1,     // High temperature
            135,      // High pulse rate
            30,       // High respiratory rate
            criticalBP,
            88.0,     // Low oxygen saturation
            160.0,    // Height (cm)
            95.0      // Weight (kg)
        );
        criticalVitals.setPainLevel(VitalSigns.PainLevel.SEVERE); // Assuming this is a setter in your class
    
        // Manually trigger alert using what's actually in VitalSignsAlertSystem
        service.triggerAlert(
            patient.getPatientID(),            // patientId
            criticalVitals,                    // vitals
            patient.getPrimaryDoctor().getEmail(),   // Assuming patient has a method like this
            patient.getPrimaryDoctor() .getEmail()   // Assuming patient has a method like this
        );
        System.out.println("Vital signs alert triggered for patient.");
    }
    

    private static void testMedicalRecords(Doctor doctor, Patient patient) {
        System.out.println("\n=== Testing Medical Records ===");
    
        // Create a medical record for the patient
        MedicalRecord record = new MedicalRecord();
    
        // Add some information to the medical record
        record.addDiagnosis(new MedicalRecord.Diagnosis(
            "Z01.419", "Annual physical", LocalDate.now(), doctor, false
        ));
        record.addClinicalNote(new MedicalRecord.ClinicalNote(
            LocalDate.now(), doctor.getFullName(), "Patient in good health. Recommended exercise and diet."
        ));
        Prescription.Medication medication = new Prescription.Medication(
            "Aspirin", // name
            "Acetylsalicylic Acid", // generic name
            "1234-5678-90", // NDC code
            Prescription.MedicationClass.ANALGESIC, // medication class
            "Tablet", // form
            "500 mg" // strength
        );
        
        // Step 2: Create Dosage instance
        Prescription.Dosage dosage = new Prescription.Dosage(
            1.0, // amount
            "tablet", // unit
            Prescription.Frequency.QD, // frequency
            "oral" // route
        );

        // Step 3: Create RefillInfo instance
        Prescription.RefillInfo refillInfo = new Prescription.RefillInfo(
            3, // authorized refills
            2 // remaining refills
        );

        // Step 4: Create Prescription instance
        Prescription prescription = new Prescription(
            "RX12345678", // prescription ID (must follow format "RX" followed by 8 digits)
            medication, // medication
            null, // prescribing doctor (you may need to provide a valid Doctor instance here)
            LocalDate.now(), // date prescribed
            LocalDate.now(), // start date
            dosage, // dosage
            "Take one tablet daily", // instructions
            refillInfo, // refill info
            "Pain relief" // reason for prescription
        );
        // Example of adding a prescription
        record.addMedication(prescription);
    
        // Display the created medical record information
        System.out.println("Created medical record: Annual Physical Exam");
    
        System.out.println("Patient's medical history:");
        // For simplicity, we are displaying the diagnosis and clinical notes for now
        record.getAllDiagnoses().forEach(d -> 
            System.out.println("- " + d.getDateDiagnosed() + ": " + d.getDescription())
        );
        record.getAllClinicalNotes().forEach(c -> 
            System.out.println("- " + c.getDate() + ": " + c.getContent())
        );
    }
    
    private static void shutdownServices(ChatServer chatServer, VideoCallService videoService) {
        chatServer.shutdown();
        videoService.shutdown();
        System.out.println("\nAll services shut down");
    }
}