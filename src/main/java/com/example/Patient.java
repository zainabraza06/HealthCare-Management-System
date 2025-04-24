package com.example;

import java.time.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Patient extends User {
    private final String PatientID;
    private final String medicalRecordNumber;
    private final MedicalRecord medicalRecord;
    private final List<Appointment> appointments;
    private final AppointmentManager appointmentManager;
    private final VitalDatabase vitalDatabase;
    private Doctor primaryDoctor;
    private final ChatServer chatServer;
    
    private String location;

    private static final Pattern MRN_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,12}$");

    public enum PaymentMethod { CASH, CREDIT_CARD, INSURANCE, BANK_TRANSFER }

    public Patient(String userId, String firstName, String lastName, String email,
                   String password, LocalDate dateOfBirth, String address,
                   String phoneNumber, Gender gender, String nationality,
                   User.EmergencyContact emergencyContact, BloodType bloodType,
                   String identificationNumber, String preferredLanguage,
                   String profileImageUrl, String patientId, String medicalRecordNumber
                    , MedicalRecord medicalRecord, Doctor primaryDoctor,
                   AppointmentManager appointmentManager,String location, VitalDatabase database,
                   FeedbackManager fdbackmanager, ChatServer chatServer) {

        super(userId, firstName, lastName, email, Role.PATIENT, password,
              dateOfBirth, address, phoneNumber, gender, nationality,
              emergencyContact, bloodType, identificationNumber,
              preferredLanguage, profileImageUrl, fdbackmanager);
        this.primaryDoctor=primaryDoctor;
        this.PatientID = patientId != null ? patientId : "P-" + UUID.randomUUID().toString().substring(0, 8);
        this.medicalRecordNumber = validateMedicalRecordNumber(medicalRecordNumber);

        this.medicalRecord = medicalRecord != null ? medicalRecord : new MedicalRecord();
        this.appointments = new CopyOnWriteArrayList<>();
        this.appointmentManager = appointmentManager;
        this.vitalDatabase = database;
        this.location=location;
        this.chatServer=chatServer;
    }

    private String validateMedicalRecordNumber(String number) {
        return validate(number, MRN_PATTERN, "MRN must be 8-12 alphanumeric characters");
    }
    public void requestPrescription(Doctor doctor) {
        System.out.printf("%s requesting prescription from Dr. %s%n", getFullName(), doctor.getFullName());
        this.connectToChat(chatServer);
        this.sendMessage(doctor.getDoctorID(), "Hello Doctor, I need a prescription");
    }
    
    //getters 
   public Doctor getPrimaryDoctor() {return this.primaryDoctor;}
   public void setPrimaryDoctor(Doctor doctor){this.primaryDoctor=doctor;}
    public String getPatientID() { return this.PatientID; }
    public String getCurrentLocation() {return this.location;}
    public String getMedicalRecordNumber() { return medicalRecordNumber; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public List<Appointment> getAppointments() { return Collections.unmodifiableList(appointments); }


   
    //recording vital Signs

    public void recordVitalSigns(VitalSigns vitalSigns) {
        Objects.requireNonNull(vitalSigns, "Vital signs cannot be null");
        vitalDatabase.recordVitals(this, vitalSigns);
    }



    public Optional<VitalSigns> getLatestVitals() {
        return vitalDatabase.getLatestVitals(this.PatientID);
    }

    public List<VitalSigns> getVitalsBetween(LocalDate from, LocalDate to) {
        return vitalDatabase.getVitalsInRange(this.PatientID, from, to);
    }

    public Set<LocalDate> getRecordedVitalDates() {
        return vitalDatabase.getRecordedDates(this.PatientID);
    }

    public Map<String, Double> getVitalTrends(VitalSigns.Component component, Period lookbackPeriod) {
        return vitalDatabase.calculateTrends(this.PatientID, component, lookbackPeriod);
    }



    //working with Medical Record

    public void addPrescription(Prescription prescription) {
        Objects.requireNonNull(prescription, "Prescription cannot be null");
        medicalRecord.addMedication(prescription);
    }

    public void addProcedure(MedicalRecord.Procedure procedure) { medicalRecord.addProcedure(procedure); }
    public void addImmunization(MedicalRecord.Immunization immunization) { medicalRecord.addImmunization(immunization); }
    public void addClinicalNote(MedicalRecord.ClinicalNote note) { medicalRecord.addClinicalNote(note); }
    public void addLabResult(MedicalRecord.LabResult result) { medicalRecord.addLabResult(result); }
    public void addHospitalization(MedicalRecord.Hospitalization hospitalization) { medicalRecord.addHospitalization(hospitalization); }
    public void addAllergy(MedicalRecord.Allergy allergy) { medicalRecord.addAllergy(allergy); }
    public void addDiagnosis(MedicalRecord.Diagnosis condition) { medicalRecord.addDiagnosis(condition); }


    //Working with Appointments

    public List<Appointment> viewUpcomingAppointments() {
        return appointmentManager.getPatientSchedule(PatientID, LocalDate.now()).stream()
            .filter(appointment -> appointment.getStatus() == Appointment.Status.CONFIRMED)
            .collect(Collectors.toList());
    }

    public List<Appointment> viewAppointmentHistory() {
        return appointmentManager.getPatientSchedule(PatientID, LocalDate.now().minusYears(5)).stream()
            .filter(appointment -> List.of(Appointment.Status.COMPLETED, Appointment.Status.CANCELLED, Appointment.Status.NO_SHOW).contains(appointment.getStatus()))
            .collect(Collectors.toList());
    }

    public void cancelAppointment(String appointmentId, String reason) {
        appointmentManager.cancelAppointmentByPatient(PatientID, appointmentId, reason);
    }

    public Appointment requestAppointment(String doctorId, LocalDateTime dateTime, Duration duration, String reason, String location) {
        return appointmentManager.scheduleAppointment(PatientID, doctorId, dateTime, duration, reason, location);
    }

    public Appointment rescheduleAppointment(String appointmentId, LocalDateTime newDateTime, Duration newDuration) {
        return appointmentManager.rescheduleAppointment(appointmentId, newDateTime, newDuration, "Patient");
    }

    public List<Appointment> viewTodaysAppointments() {
        return appointmentManager.getPatientSchedule(PatientID, LocalDate.now());
    }

    



public String getID(){
    return this.PatientID;
}


    public List<Prescription> getActivePrescriptions() {
        return medicalRecord.getActiveMedications();
    }

   
    public List<Prescription> getPrescriptions() {
        return medicalRecord.getAllPrescription();
    }


    //working with feedbacks 


    public void submitFeedback(Feedback feedback) {
            FeedbackManager.submitFeedback(feedback);
            System.out.println("Feedback submitted successfully by " + this.getFullName());
       
    }

    //abstract method overriding 
    public void displayInfo() {
        System.out.println("\n=== PATIENT DETAILS ===");
        System.out.printf("MRN: %s | Name: %s %s\n", medicalRecordNumber, getFirstName(), getLastName());
        System.out.printf("Age: %d | DOB: %s | Gender: %s\n", calculateAge(), getDateOfBirth(), getGender());
       
        System.out.println("\n=== MEDICAL SUMMARY ===");
        System.out.println("Active Prescriptions: " + getActivePrescriptions().size());
        getLatestVitals().ifPresent(vs -> System.out.println("Latest Vital Signs: " + vs));

        System.out.println("\n=== NEXT APPOINTMENT ===");
        viewUpcomingAppointments().stream().findFirst().ifPresentOrElse(
            System.out::println,
            () -> System.out.println("No upcoming appointments")
        );
    }
}
