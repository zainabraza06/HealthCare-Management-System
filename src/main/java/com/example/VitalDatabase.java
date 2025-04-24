package com.example;


import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class VitalDatabase {
    private final ConcurrentMap<String, ConcurrentNavigableMap<LocalDate, VitalSigns>> database;
    private final int maxEntriesPerPatient;
    private final VitalSignsAlertSystem alertSystem;

    // Constructor with full initialization
    public VitalDatabase(int maxEntriesPerPatient, VitalSignsAlertSystem alertSystem) {
        if (maxEntriesPerPatient <= 0) {
            throw new IllegalArgumentException("Max entries per patient must be positive");
        }
        
        this.database = new ConcurrentHashMap<>();
        this.maxEntriesPerPatient = maxEntriesPerPatient;
        this.alertSystem = alertSystem;
    }



    //to Record Vitals 

    public boolean recordVitals(Patient patient, VitalSigns vitals) {
        Objects.requireNonNull(patient.getID(), "Patient ID cannot be null");
        Objects.requireNonNull(vitals, "Vital signs cannot be null");
    
        LocalDate recordDate = LocalDate.now();
    
        ConcurrentNavigableMap<LocalDate, VitalSigns> patientRecords =
            database.computeIfAbsent(patient.getID(), k -> new ConcurrentSkipListMap<>(Comparator.reverseOrder()));
    
        if (patientRecords.containsKey(recordDate)) return false;
    
        if (patientRecords.size() >= maxEntriesPerPatient) {
            patientRecords.pollLastEntry();
        }
    
        patient.getMedicalRecord().addVitalSigns(recordDate, vitals);
        patientRecords.put(recordDate, vitals);
    
        if (vitals.isCritical()) {
            Doctor doctor = patient.getPrimaryDoctor();
            if (doctor != null) {
                alertSystem.triggerAlert(
                    patient.getID(),
                    vitals,
                    doctor.getEmail(),
                    doctor.getPhoneNumber()
                );
            } else {
                System.err.println("No primary doctor assigned to patient: " + patient.getID());
            }
        }
    
        return true;
    }
    





    // Retrieves the most recent vital signs for a patient.
     
    public Optional<VitalSigns> getLatestVitals(String patientId) {
        ConcurrentNavigableMap<LocalDate, VitalSigns> records = database.get(patientId);
        return (records != null && !records.isEmpty()) ? Optional.of(records.firstEntry().getValue()) : Optional.empty();
    }





    // Retrieves vital signs history within a date range.
    
    public List<VitalSigns> getVitalsInRange(String patientId, LocalDate start, LocalDate end) {
        ConcurrentNavigableMap<LocalDate, VitalSigns> records = database.get(patientId);
        return (records != null) ? 
            records.subMap(start, true, end, true).values().stream().collect(Collectors.toList()) : Collections.emptyList();
    }





    // Gets all recorded dates for a patient's vitals.
     
    public Set<LocalDate> getRecordedDates(String patientId) {
        ConcurrentNavigableMap<LocalDate, VitalSigns> records = database.get(patientId);
        return (records != null) ? records.keySet() : Collections.emptySet();
    }




    
    
    // Calculates trend statistics for a specific vital sign component.
     
    public Map<String, Double> calculateTrends(String patientId, VitalSigns.Component component, Period lookbackPeriod) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(lookbackPeriod);
        
        List<VitalSigns> vitals = getVitalsInRange(patientId, startDate, endDate);
        
        return Map.of(
            "average", calculateAverage(vitals, component),
            "min", calculateMin(vitals, component),
            "max", calculateMax(vitals, component),
            "latest", getLatestValue(vitals, component)
        );
    }




    //helper functions to get analysis
    private double calculateAverage(List<VitalSigns> vitals, VitalSigns.Component component) {
        return vitals.stream()
            .mapToDouble(v -> v.getComponent(component))
            .average()
            .orElse(0.0);
    }

    private double calculateMin(List<VitalSigns> vitals, VitalSigns.Component component) {
        return vitals.stream()
            .mapToDouble(v -> v.getComponent(component))
            .min()
            .orElse(0.0);
    }

    private double calculateMax(List<VitalSigns> vitals, VitalSigns.Component component) {
        return vitals.stream()
            .mapToDouble(v -> v.getComponent(component))
            .max()
            .orElse(0.0);
    }

    private double getLatestValue(List<VitalSigns> vitals, VitalSigns.Component component) {
        return vitals.stream()
            .findFirst()
            .map(v -> v.getComponent(component))
            .orElse(0.0);
    }


    
   
    }


