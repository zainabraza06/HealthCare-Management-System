package com.example;


import java.time.LocalDate;
import java.util.*;


public class MedicalRecord {
    private final List<Diagnosis> diagnoses;
    private final List<Procedure> procedures;
    private final List<Prescription> medicationHistory;
    private final Map<LocalDate, VitalSigns> vitalSignsArchive;
    private final List<Allergy> allergies;
    private final List<Immunization> immunizations;
    private final List<ClinicalNote> clinicalNotes;
    private final List<LabResult> labResults;
    private final List<Hospitalization> hospitalizations;

    public MedicalRecord() {
        this.diagnoses = new ArrayList<>();
        this.procedures = new ArrayList<>();
        this.medicationHistory = new ArrayList<>();
        this.vitalSignsArchive = new TreeMap<>(Collections.reverseOrder());
        this.allergies = new ArrayList<>();
        this.immunizations = new ArrayList<>();
        this.clinicalNotes = new ArrayList<>();
        this.labResults = new ArrayList<>();
        this.hospitalizations = new ArrayList<>();
    }

    public List<ClinicalNote> getAllClinicalNotes() {return this.clinicalNotes;};

    // ================= RECORD MANAGEMENT =================
   public void addDiagnosis(Diagnosis diagnosis) {
    Objects.requireNonNull(diagnosis, "Diagnosis cannot be null");
    if (diagnoses.contains(diagnosis)) {
        throw new IllegalArgumentException("Diagnosis already recorded");
    }
    diagnoses.add(diagnosis);
}

    public void addProcedure(Procedure procedure) {
        procedures.add(Objects.requireNonNull(procedure));
    }

    public void addMedication(Prescription prescription) {
        medicationHistory.add(Objects.requireNonNull(prescription));
    }

    public void addVitalSigns(LocalDate date, VitalSigns vitalSigns) {
        vitalSignsArchive.put(Objects.requireNonNull(date), Objects.requireNonNull(vitalSigns));
    }

    public void addAllergy(Allergy allergy) {
        Objects.requireNonNull(allergy, "Allergy cannot be null");
    
        if (allergies.stream().anyMatch(existing ->
            existing.getSubstance().equalsIgnoreCase(allergy.getSubstance()) &&
            existing.getReaction().equalsIgnoreCase(allergy.getReaction()) &&
            existing.getSeverity() == allergy.getSeverity())) {
            
            throw new IllegalArgumentException("Duplicate allergy already recorded.");
        }
    
        allergies.add(allergy);
    }
    

    public void addImmunization(Immunization immunization) {
        immunizations.add(Objects.requireNonNull(immunization));
    }

    public void addClinicalNote(ClinicalNote note) {
        clinicalNotes.add(Objects.requireNonNull(note));
    }

    public void addLabResult(LabResult result) {
        labResults.add(Objects.requireNonNull(result));
    }

    public void addHospitalization(Hospitalization hospitalization) {
        hospitalizations.add(Objects.requireNonNull(hospitalization));
    }

    // ================= QUERY METHODS =================
    public List<Diagnosis> getActiveDiagnoses() {
        return diagnoses.stream()
                .filter(d -> d.isChronic() || 
                        d.getDateDiagnosed().isAfter(LocalDate.now().minusYears(1)))
                .toList();
    }

    public Optional<VitalSigns> getLatestVitalSigns() {
        return vitalSignsArchive.isEmpty() ? 
                Optional.empty() : 
                Optional.of(vitalSignsArchive.values().iterator().next());
    }

    public List<Prescription> getActiveMedications() {
        // Check the 'status' of the Prescription rather than using 'isActive'
        return medicationHistory.stream()
                .filter(p -> p.getStatus() == Prescription.PrescriptionStatus.ACTIVE)
                .toList();
    }

    public boolean hasAllergyTo(String substance) {
        return allergies.stream()
                .anyMatch(a -> a.getSubstance().equalsIgnoreCase(substance));
    }

    // ================= IMMUTABLE VIEWS =================
    public List<Diagnosis> getAllDiagnoses() {
        return Collections.unmodifiableList(diagnoses);
    }
   
    public List<Prescription> getAllPrescription(){
       return Collections.unmodifiableList(medicationHistory);
    }
    public List<Procedure> getAllProcedures() {
        return Collections.unmodifiableList(procedures);
    }

    public Map<LocalDate, VitalSigns> getVitalSignsHistory() {
        return Collections.unmodifiableMap(vitalSignsArchive);
    }

    public List<Allergy> getAllAllergies() {
        return Collections.unmodifiableList(allergies);
    }

    // ================= SUMMARY METHODS =================
    public String generateMedicalSummary() {
        return String.format(
            "Medical Summary:%n" +
            "- Active Diagnoses: %d%n" +
            "- Active Medications: %d%n" +
            "- Known Allergies: %d%n" +
            "- Recent Vital Signs: %s",
            getActiveDiagnoses().size(),
            getActiveMedications().size(),
            allergies.size(),
            getLatestVitalSigns().map(VitalSigns::toString).orElse("Not available")
        );
    }

    // ================= SUPPORTING CLASSES =================
    public static class Allergy {
        private final String substance;
        private final String reaction;
        private final Severity severity;

        public Allergy(String substance, String reaction, Severity severity) {
            this.substance = Objects.requireNonNull(substance);
            this.reaction = Objects.requireNonNull(reaction);
            this.severity = Objects.requireNonNull(severity);
        }

        public enum Severity { MILD, MODERATE, SEVERE, LIFE_THREATENING }

        // Getters
        public String getSubstance() { return substance; }
        public String getReaction() { return reaction; }
        public Severity getSeverity() { return severity; }
    }

    public static class Immunization {
        private final String vaccine;
        private final LocalDate dateAdministered;
        private final LocalDate expirationDate;
        private final String administeringEntity;

        public Immunization(String vaccine, LocalDate dateAdministered,
                          LocalDate expirationDate, String administeringEntity) {
            this.vaccine = Objects.requireNonNull(vaccine);
            this.dateAdministered = Objects.requireNonNull(dateAdministered);
            this.expirationDate = expirationDate;
            this.administeringEntity = administeringEntity;
        }

        // Getters
        public String getVaccine() { return vaccine; }
        public LocalDate getDateAdministered() { return dateAdministered; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public String getAdministeringEntity() { return administeringEntity; }
    }

    public static class ClinicalNote {
        private final LocalDate date;
        private final String author;
        private final String content;

        public ClinicalNote(LocalDate date, String author, String content) {
            this.date = Objects.requireNonNull(date);
            this.author = Objects.requireNonNull(author);
            this.content = Objects.requireNonNull(content);
        }

        // Getters
        public LocalDate getDate() { return date; }
        public String getAuthor() { return author; }
        public String getContent() { return content; }
    }

    public static class Hospitalization {
        private final LocalDate admissionDate;
        private final LocalDate dischargeDate;
        private final String reason;
        private final String facility;
        private final String attendingPhysician;

        public Hospitalization(LocalDate admissionDate, LocalDate dischargeDate,
                             String reason, String facility, String attendingPhysician) {
            this.admissionDate = Objects.requireNonNull(admissionDate);
            this.dischargeDate = dischargeDate;
            this.reason = Objects.requireNonNull(reason);
            this.facility = facility;
            this.attendingPhysician = attendingPhysician;
        }

        // Getters
        public LocalDate getAdmissionDate() { return admissionDate; }
        public LocalDate getDischargeDate() { return dischargeDate; }
        public String getReason() { return reason; }
        public String getFacility() { return facility; }
        public String getAttendingPhysician() { return attendingPhysician; }
    }

     // ================= CORE MEDICAL ENTITIES =================
     public static class Diagnosis {
        private final String code;
        private final String description;
        private final LocalDate dateDiagnosed;
        private final Doctor doctor;
        private final boolean chronic;

        public Diagnosis(String code, String description, LocalDate dateDiagnosed, 
                        Doctor doctor, boolean chronic) {
            this.code = Objects.requireNonNull(code);
            this.description = Objects.requireNonNull(description);
            this.dateDiagnosed = Objects.requireNonNull(dateDiagnosed);
            this.doctor = doctor;
            this.chronic = chronic;
        }

        // Getters
        public String getCode() { return code; }
        public String getDescription() { return description; }
        public LocalDate getDateDiagnosed() { return dateDiagnosed; }
        public Doctor getPhysician() { return doctor; }
        public boolean isChronic() { return chronic; }
    }

    public static class Procedure {
        private final String code;
        private final String description;
        private final LocalDate datePerformed;
        private final Doctor doctor;
        private final String facility;

        public Procedure(String code, String description, LocalDate datePerformed,
                        Doctor doctor, String facility) {
            this.code = Objects.requireNonNull(code);
            this.description = Objects.requireNonNull(description);
            this.datePerformed = Objects.requireNonNull(datePerformed);
            this.doctor = doctor;
            this.facility = facility;
        }

        // Getters
        public String getCode() { return code; }
        public String getDescription() { return description; }
        public LocalDate getDatePerformed() { return datePerformed; }
        public Doctor getPerformingPhysician() { return doctor; }
        public String getFacility() { return facility; }
    }
    public static class LabResult {
        private final String testName;
        private final String testCode;
        private final LocalDate testDate;
        private final String result;
        private final String unit;
        private final String normalRange;
        private final String labFacility;
        private final String performingPhysician;
    
        public LabResult(String testName, String testCode, LocalDate testDate, 
                         String result, String unit, String normalRange, 
                         String labFacility, String performingPhysician) {
            this.testName = Objects.requireNonNull(testName);
            this.testCode = Objects.requireNonNull(testCode);
            this.testDate = Objects.requireNonNull(testDate);
            this.result = Objects.requireNonNull(result);
            this.unit = unit; // Can be null if not applicable
            this.normalRange = normalRange; // Can be null if not provided
            this.labFacility = labFacility; // Can be null if not specified
            this.performingPhysician = performingPhysician; // Can be null if not specified
        }
    
        // Getters
        public String getTestName() { return testName; }
        public String getTestCode() { return testCode; }
        public LocalDate getTestDate() { return testDate; }
        public String getResult() { return result; }
        public String getUnit() { return unit; }
        public String getNormalRange() { return normalRange; }
        public String getLabFacility() { return labFacility; }
        public String getPerformingPhysician() { return performingPhysician; }
    
        @Override
        public String toString() {
            return String.format("Lab Test: %s (%s) %nDate: %s %nResult: %s %s %nNormal Range: %s %nPerformed by: %s",
                    testName, testCode, testDate, result, unit != null ? unit : "", 
                    normalRange != null ? normalRange : "N/A", 
                    performingPhysician != null ? performingPhysician : "Unknown");
        }
    }
    
}

