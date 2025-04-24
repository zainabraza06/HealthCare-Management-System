package com.example;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a medication prescription with full tracking capabilities.
 */
class Prescription {
    private String prescriptionId;
    private Medication medication;
    private Doctor prescribingDoctor;
    private LocalDate datePrescribed;
    private LocalDate startDate;
    private LocalDate endDate;
    private Dosage dosage;
    private String instructions;
    private RefillInfo refillInfo;
    private PrescriptionStatus status;
    private String pharmacyDetails;
    private String reasonForPrescription;
    private boolean isDispensed;

    public Medication getMedication() {
        return medication;
    }
    
    public Dosage getDosage() {
        return dosage;
    }
    
    public Doctor getPrescribingDoctor() {
        return prescribingDoctor;
    }

    public String getID(){return prescriptionId;}

    // ================= CONSTRUCTOR =================
    public Prescription(String prescriptionId, Medication medication, 
                      Doctor prescribingDoctor, LocalDate datePrescribed,
                      LocalDate startDate, Dosage dosage, String instructions,
                      RefillInfo refillInfo, String reasonForPrescription) {
        
        // Use setters to initialize values and validate them
        setPrescriptionId(prescriptionId);
        setMedication(medication);
        setPrescribingDoctor(prescribingDoctor);
        setDatePrescribed(datePrescribed);
        setStartDate(startDate);
        setDosage(dosage);
        setInstructions(instructions);
        setRefillInfo(refillInfo);
        setStatus(PrescriptionStatus.ACTIVE);  // Default status is ACTIVE
        setReasonForPrescription(reasonForPrescription);
        setIsDispensed(false);  // Default dispensed flag is false
        validateDates(); // Ensures startDate and endDate are valid
    }

    // ================= VALIDATION =================
    private void validateDates() {
        if (startDate.isBefore(datePrescribed)) {
            throw new IllegalArgumentException("Start date cannot be before prescription date");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    // ================= SETTERS WITH VALIDATION =================
    public void setPrescriptionId(String prescriptionId) {
        if (prescriptionId == null || prescriptionId.trim().isEmpty() || !prescriptionId.matches("RX\\d{8}")) {
            throw new IllegalArgumentException("Invalid prescription ID format");
        }
        this.prescriptionId = prescriptionId;
    }

    public void setMedication(Medication medication) {
        this.medication = Objects.requireNonNull(medication, "Medication cannot be null");
    }

    public void setPrescribingDoctor(Doctor prescribingDoctor) {
        this.prescribingDoctor = prescribingDoctor;
    }

    public void setDatePrescribed(LocalDate datePrescribed) {
        this.datePrescribed = Objects.requireNonNull(datePrescribed);
    }

    public void setStartDate(LocalDate startDate) {
        if (startDate.isBefore(datePrescribed)) {
            throw new IllegalArgumentException("Start date cannot be before prescription date");
        }
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.endDate = endDate;
    }

    public void setDosage(Dosage dosage) {
        this.dosage = Objects.requireNonNull(dosage);
    }

    public void setInstructions(String instructions) {
        this.instructions = (instructions != null) ? instructions : "Take as directed";
    }

    public void setRefillInfo(RefillInfo refillInfo) {
        this.refillInfo = (refillInfo != null) ? refillInfo : new RefillInfo(0, 0);
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public void setReasonForPrescription(String reasonForPrescription) {
        this.reasonForPrescription = (reasonForPrescription != null) ? reasonForPrescription : "";
    }

    public void setIsDispensed(boolean isDispensed) {
        this.isDispensed = isDispensed;
    }

    // ================= CORE METHODS =================
    public void dispense(String pharmacyDetails) {
        if (isDispensed) {
            throw new IllegalStateException("Prescription already dispensed");
        }
        this.pharmacyDetails = Objects.requireNonNull(pharmacyDetails);
        this.isDispensed = true;
    }

    public void refill() {
        if (!isDispensed) {
            throw new IllegalStateException("Prescription must be dispensed first");
        }
        if (refillInfo.remainingRefills() <= 0) {
            throw new IllegalStateException("No refills remaining");
        }
        refillInfo = refillInfo.useRefill();
        this.isDispensed = false;
        this.pharmacyDetails = null;
    }

    public void updateStatus(PrescriptionStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus);
        if (newStatus == PrescriptionStatus.COMPLETED || 
            newStatus == PrescriptionStatus.CANCELLED) {
            this.endDate = LocalDate.now();
        }
    }

    public boolean isActive() {
        return status == PrescriptionStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }
    public PrescriptionStatus getStatus() {
        return status;
    }

    // ================= NESTED CLASSES =================

    public static final class Medication {
        private final String name;
        private final String genericName;
        private final String ndcCode;
        private final MedicationClass medicationClass;
        private final String form; // tablet, capsule, liquid, etc.
        private final String strength;

        public Medication(String name, String genericName, String ndcCode,
                         MedicationClass medicationClass, String form, String strength) {
            this.name = Objects.requireNonNull(name);
            this.genericName = (genericName != null) ? genericName : name;
            this.ndcCode = validateNdc(ndcCode);
            this.medicationClass = Objects.requireNonNull(medicationClass);
            this.form = Objects.requireNonNull(form);
            this.strength = Objects.requireNonNull(strength);
        }

        private String validateNdc(String ndc) {
            if (ndc == null || !ndc.matches("\\d{4}-\\d{4}-\\d{2}")) {
                throw new IllegalArgumentException("Invalid NDC format");
            }
            return ndc;
        }

        // Getters
        public String getName() { return name; }
        public String getGenericName() { return genericName; }
        public String getNdcCode() { return ndcCode; }
        public MedicationClass getMedicationClass() { return medicationClass; }
        public String getForm() { return form; }
        public String getStrength() { return strength; }
    }

    public record Dosage(
        double amount,
        String unit,  // mg, mL, etc.
        Frequency frequency,
        String route  // oral, IV, topical, etc.
    ) {
        public Dosage {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            Objects.requireNonNull(unit);
            Objects.requireNonNull(frequency);
            Objects.requireNonNull(route);
        }
    }

    public record RefillInfo(
        int authorizedRefills,
        int remainingRefills
    ) {
        public RefillInfo {
            if (authorizedRefills < 0 || remainingRefills < 0) {
                throw new IllegalArgumentException("Refill counts cannot be negative");
            }
            if (remainingRefills > authorizedRefills) {
                throw new IllegalArgumentException("Remaining refills cannot exceed authorized");
            }
        }

        public RefillInfo useRefill() {
            return new RefillInfo(authorizedRefills, remainingRefills - 1);
        }
    }

    public enum Frequency {
        PRN("As needed"),
        QD("Once daily"),
        BID("Twice daily"),
        TID("Three times daily"),
        QID("Four times daily"),
        QHS("At bedtime"),
        QOD("Every other day"),
        QWK("Once weekly");

        private final String description;

        Frequency(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum MedicationClass {
        ANALGESIC, ANTIBIOTIC, ANTIHISTAMINE, ANTICOAGULANT,
        ANTIDEPRESSANT, ANTIDIABETIC, ANTIHYPERTENSIVE, BRONCHODILATOR,
        DIURETIC, STEROID, SEDATIVE, VACCINE, OTHER
    }

    public enum PrescriptionStatus {
        ACTIVE, COMPLETED, CANCELLED, EXPIRED, ON_HOLD
    }

    // ================= TO STRING =================
    @Override
    public String toString() {
        return String.format(
            "Prescription[ID=%s, Medication=%s, Status=%s, Start=%s, End=%s, Dosage=%s, Instructions=%s, " +
            "RefillInfo=%s, Reason=%s, Dispensed=%b, PharmacyDetails=%s]",
            prescriptionId, medication.getName(), status, startDate, endDate, dosage, instructions,
            refillInfo, reasonForPrescription, isDispensed, pharmacyDetails
        );
    }
}

